package com.aistudio.richfarmaciw.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AccountState(
    val familyCode: String = "",
    val isConnected: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val accountName: String = "",
    val syncErrorMessage: String = ""
)

@Serializable
data class CloudAccountPayload(
    val familyCode: String = "",
    val accountName: String = "",
    val lastUpdated: Long = 0L,
    val backupData: BackupData = BackupData()
)
