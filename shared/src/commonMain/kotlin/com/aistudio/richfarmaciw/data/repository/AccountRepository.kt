package com.aistudio.richfarmaciw.data.repository

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.AccountState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountRepository(private val db: RichFarmaciDatabase) {

    private val _accountState = MutableStateFlow(AccountState())
    val accountState: StateFlow<AccountState> = _accountState.asStateFlow()

    suspend fun loadAccountState(): AccountState {
        var code = db.getMetadataValue(KEY_FAMILY_CODE)
        if (code.isNullOrBlank()) {
            code = DEFAULT_FAMILY_CODE
            db.setMetadataValue(KEY_FAMILY_CODE, DEFAULT_FAMILY_CODE)
            db.setMetadataValue(KEY_IS_CONNECTED, "true")
        }

        val isConnStr = db.getMetadataValue(KEY_IS_CONNECTED)
        val isConn = isConnStr == null || isConnStr == "true"
        val lastSync = db.getMetadataValue(KEY_LAST_SYNC_TS)?.toLongOrNull() ?: 0L
        val name = db.getMetadataValue(KEY_ACCOUNT_NAME) ?: ""

        val newState = AccountState(
            familyCode = code,
            isConnected = isConn,
            lastSyncTimestamp = lastSync,
            accountName = name
        )
        _accountState.value = newState
        return newState
    }

    suspend fun createNewAccount(accountName: String = ""): String {
        val newCode = generateFamilyCode()
        db.setMetadataValue(KEY_FAMILY_CODE, newCode)
        db.setMetadataValue(KEY_IS_CONNECTED, "true")
        db.setMetadataValue(KEY_ACCOUNT_NAME, accountName)
        loadAccountState()
        return newCode
    }

    suspend fun joinAccount(familyCode: String, accountName: String = "") {
        val cleanCode = if (familyCode.isBlank()) DEFAULT_FAMILY_CODE else familyCode.trim()
        db.setMetadataValue(KEY_FAMILY_CODE, cleanCode)
        db.setMetadataValue(KEY_IS_CONNECTED, "true")
        if (accountName.isNotBlank()) {
            db.setMetadataValue(KEY_ACCOUNT_NAME, accountName)
        }
        loadAccountState()
    }

    suspend fun resetToDefaultAccount() {
        db.setMetadataValue(KEY_FAMILY_CODE, DEFAULT_FAMILY_CODE)
        db.setMetadataValue(KEY_IS_CONNECTED, "true")
        db.deleteMetadataKey(KEY_LAST_SYNC_TS)
        loadAccountState()
    }

    suspend fun disconnectAccount() {
        db.deleteMetadataKey(KEY_FAMILY_CODE)
        db.setMetadataValue(KEY_IS_CONNECTED, "false")
        db.deleteMetadataKey(KEY_LAST_SYNC_TS)
        loadAccountState()
    }

    suspend fun updateLastSync(timestamp: Long) {
        db.setMetadataValue(KEY_LAST_SYNC_TS, timestamp.toString())
        loadAccountState()
    }

    suspend fun setError(message: String) {
        _accountState.value = _accountState.value.copy(syncErrorMessage = message)
    }

    private fun generateFamilyCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part1 = (1..3).map { chars.random() }.joinToString("")
        val part2 = (1..3).map { chars.random() }.joinToString("")
        return "FARM-$part1-$part2"
    }

    companion object {
        const val DEFAULT_FAMILY_CODE = "RichFarmaci"
        private const val KEY_FAMILY_CODE = "account_family_code"
        private const val KEY_IS_CONNECTED = "account_is_connected"
        private const val KEY_LAST_SYNC_TS = "account_last_sync_ts"
        private const val KEY_ACCOUNT_NAME = "account_name"
    }
}
