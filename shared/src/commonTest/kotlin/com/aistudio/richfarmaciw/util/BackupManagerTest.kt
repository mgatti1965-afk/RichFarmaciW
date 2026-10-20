package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupManagerTest {

    @Test
    fun testBackupAndRestoreIntegrity() = runTest {
        val originalDb = FakeRichFarmaciDatabase()
        val backupManager = BackupManager(originalDb)

        // 1. Prepara i dati di test
        val profile = Profile(id = "p1", pazienteNome = "Mario Rossi", pazienteCf = "RSSMRA80A01H501Z")
        val medication = Medication(id = "m1", profileId = "p1", nome = "Tachipirina", scatole = 2)
        val sentMedications = listOf(SentMedication("Tachipirina", 1, "Per febbre"))
        val sentRequest = SentRequest(
            id = "r1",
            profileId = "p1",
            data = "2023-10-27",
            pazienteNome = "Mario Rossi",
            medicoNome = "Dr. Bianchi",
            farmaciSerialized = SentRequest.serializeMedicines(sentMedications),
            testoCompleto = "Testo della richiesta"
        )

        originalDb.insertProfile(profile)
        originalDb.insertMedication(medication)
        originalDb.insertSentRequest(sentRequest)
        originalDb.setMetadataValue("onboarding_shown", "true")
        originalDb.setMetadataValue("donation_count", "5")

        // 2. Crea il Backup
        val json = backupManager.createBackupJson()
        
        // Verifica basica che il JSON contenga dati attesi
        assertTrue(json.contains("RSSMRA80A01H501Z"), "Il JSON dovrebbe contenere il CF del paziente")
        assertTrue(json.contains("Tachipirina"), "Il JSON dovrebbe contenere il nome del farmaco")

        // 3. Esegui il Restore su un nuovo DB
        val targetDb = FakeRichFarmaciDatabase()
        val targetBackupManager = BackupManager(targetDb)
        
        val result = targetBackupManager.restoreFromJson(json)
        assertTrue(result.isSuccess, "Il ripristino dovrebbe avere successo")

        // 4. Verifica Integrità
        val restoredProfiles = targetDb.getAllProfilesSnapshot()
        assertEquals(1, restoredProfiles.size)
        assertEquals(profile.pazienteNome, restoredProfiles[0].pazienteNome)
        assertEquals(profile.pazienteCf, restoredProfiles[0].pazienteCf)

        val restoredMedications = targetDb.getAllMedicationsSnapshot()
        assertEquals(1, restoredMedications.size)
        assertEquals(medication.nome, restoredMedications[0].nome)
        assertEquals(medication.scatole, restoredMedications[0].scatole)

        val restoredRequests = targetDb.getAllSentRequestsSnapshot()
        assertEquals(1, restoredRequests.size)
        assertEquals(sentRequest.data, restoredRequests[0].data)
        
        // Verifica la deserializzazione interna dei farmaci nella richiesta
        val restoredSentMedications = restoredRequests[0].getSentMedications()
        assertEquals(1, restoredSentMedications.size)
        assertEquals("Tachipirina", restoredSentMedications[0].nome)
        assertEquals("Per febbre", restoredSentMedications[0].note)

        // Verifica Metadati
        assertEquals("true", targetDb.getMetadataValue("onboarding_shown"))
        assertEquals("5", targetDb.getMetadataValue("donation_count"))
    }
}

/**
 * Un database fake in memoria per testare la logica del BackupManager 
 * senza dipendere dall'implementazione Room specifica della piattaforma.
 */
class FakeRichFarmaciDatabase : RichFarmaciDatabase {
    private val profiles = mutableMapOf<String, Profile>()
    private val medications = mutableMapOf<String, Medication>()
    private val sentRequests = mutableMapOf<String, SentRequest>()
    private val metadata = mutableMapOf<String, String>()

    override fun getAllProfiles(): Flow<List<Profile>> = flowOf(profiles.values.toList())
    override suspend fun getProfileById(id: String): Profile? = profiles[id]
    override suspend fun insertProfile(profile: Profile) { profiles[profile.id] = profile }
    override suspend fun updateProfile(profile: Profile) { profiles[profile.id] = profile }
    override suspend fun deleteProfile(profile: Profile) { profiles.remove(profile.id) }
    override suspend fun getAllProfilesSnapshot(): List<Profile> = profiles.values.toList()

    override fun getMedicationsByProfile(profileId: String): Flow<List<Medication>> = 
        flowOf(medications.values.filter { it.profileId == profileId })
    override suspend fun getMedicationsSnapshotByProfile(profileId: String): List<Medication> = 
        medications.values.filter { it.profileId == profileId }
    override suspend fun getAllMedicationsSnapshot(): List<Medication> = medications.values.toList()
    override suspend fun getMedicationById(id: String): Medication? = medications[id]
    override suspend fun insertMedication(medication: Medication) { medications[medication.id] = medication }
    override suspend fun insertAllMedications(medications: List<Medication>) { medications.forEach { this.medications[it.id] = it } }
    override suspend fun updateMedication(medication: Medication) { medications[medication.id] = medication }
    override suspend fun deleteMedication(medication: Medication) { medications.remove(medication.id) }
    override suspend fun deleteMedicationById(id: String) { medications.remove(id) }

    override fun getSentRequestsByProfile(profileId: String): Flow<List<SentRequest>> = 
        flowOf(sentRequests.values.filter { it.profileId == profileId })
    override suspend fun insertSentRequest(sentRequest: SentRequest) { sentRequests[sentRequest.id] = sentRequest }
    override suspend fun deleteSentRequest(sentRequest: SentRequest) { sentRequests.remove(sentRequest.id) }
    override suspend fun getAllSentRequestsSnapshot(): List<SentRequest> = sentRequests.values.toList()

    override suspend fun getMetadataValue(key: String): String? = metadata[key]
    override suspend fun setMetadataValue(key: String, value: String) { metadata[key] = value }
    override suspend fun deleteMetadataKey(key: String) { metadata.remove(key) }

    override suspend fun clearAllData() {
        profiles.clear()
        medications.clear()
        sentRequests.clear()
        metadata.clear()
    }
}
