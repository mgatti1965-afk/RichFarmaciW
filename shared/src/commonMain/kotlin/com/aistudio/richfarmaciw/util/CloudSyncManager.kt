package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.BackupData
import com.aistudio.richfarmaciw.data.model.CloudAccountPayload
import com.aistudio.richfarmaciw.data.repository.AccountRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlin.time.TimeSource

class CloudSyncManager(
    private val db: RichFarmaciDatabase,
    private val accountRepository: AccountRepository
) {

    private val jsonFormatter = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonFormatter)
        }
    }

    private val backupManager = BackupManager(db)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val baseUrl = "https://richfarmaciw-default-rtdb.europe-west1.firebasedatabase.app/accounts"

    suspend fun syncNow(): Result<Unit> {
        val state = accountRepository.loadAccountState()
        val familyCode = state.familyCode.trim().uppercase()

        if (familyCode.isBlank() || !state.isConnected) {
            return Result.failure(IllegalStateException("Nessun Codice Account collegato"))
        }

        _isSyncing.value = true
        return try {
            val url = "$baseUrl/$familyCode.json"

            // 1. Leggi backup locale corrente
            val localBackupJson = backupManager.createBackupJson()
            val localBackup = jsonFormatter.decodeFromString<BackupData>(localBackupJson)

            // 2. Tenta di scaricare lo stato remoto dal Cloud
            val response = httpClient.get(url)
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()

                if (responseText.isNotBlank() && responseText != "null") {
                    val remotePayload = jsonFormatter.decodeFromString<CloudAccountPayload>(responseText)
                    val remoteBackup = remotePayload.backupData

                    // Se il cloud ha dati più recenti o elementi non presenti in locale, ripristiniamo/fondiamo
                    if (shouldMergeRemote(localBackup, remoteBackup)) {
                        backupManager.restoreFromJson(jsonFormatter.encodeToString(remoteBackup))
                    }
                }
            }

            // 3. Carica lo stato aggiornato (locale o fuso) verso il Cloud
            val currentBackupJson = backupManager.createBackupJson()
            val currentBackup = jsonFormatter.decodeFromString<BackupData>(currentBackupJson)

            val payload = CloudAccountPayload(
                familyCode = familyCode,
                accountName = state.accountName,
                lastUpdated = currentTimeMillis(),
                backupData = currentBackup
            )

            val putResponse = httpClient.put(url) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            if (putResponse.status.isSuccess()) {
                val now = currentTimeMillis()
                accountRepository.updateLastSync(now)
                accountRepository.setError("")
                Result.success(Unit)
            } else {
                val err = "Errore durante il salvataggio cloud (${putResponse.status.value})"
                accountRepository.setError(err)
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            val errMsg = "Connessione di rete non disponibile per la sincronizzazione"
            accountRepository.setError(errMsg)
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    private fun shouldMergeRemote(local: BackupData, remote: BackupData): Boolean {
        if (remote.profiles.isEmpty() && remote.medications.isEmpty()) return false
        if (local.profiles.isEmpty() && local.medications.isEmpty()) return true
        
        // Verifica se ci sono elementi del cloud non presenti in locale
        val localProfileIds = local.profiles.map { it.id }.toSet()
        val localMedicationIds = local.medications.map { it.id }.toSet()

        val hasNewRemoteProfile = remote.profiles.any { it.id !in localProfileIds }
        val hasNewRemoteMed = remote.medications.any { it.id !in localMedicationIds }

        return hasNewRemoteProfile || hasNewRemoteMed
    }

    private fun currentTimeMillis(): Long {
        return TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
    }
}
