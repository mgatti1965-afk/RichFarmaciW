package com.aistudio.richfarmaciw.data.db

import com.aistudio.richfarmaciw.data.model.*
import kotlinx.coroutines.flow.Flow

interface RichFarmaciDatabase {
    // Profiles
    fun getAllProfiles(): Flow<List<Profile>>
    suspend fun getProfileById(id: String): Profile?
    suspend fun insertProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(profile: Profile)
    suspend fun getAllProfilesSnapshot(): List<Profile>

    // Medications
    fun getMedicationsByProfile(profileId: String): Flow<List<Medication>>
    suspend fun getMedicationsSnapshotByProfile(profileId: String): List<Medication>
    suspend fun getAllMedicationsSnapshot(): List<Medication>
    suspend fun getMedicationById(id: String): Medication?
    suspend fun insertMedication(medication: Medication)
    suspend fun insertAllMedications(medications: List<Medication>)
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun deleteMedicationById(id: String)

    // Sent Requests
    fun getSentRequestsByProfile(profileId: String): Flow<List<SentRequest>>
    suspend fun insertSentRequest(sentRequest: SentRequest)
    suspend fun deleteSentRequest(sentRequest: SentRequest)

    // Metadata
    suspend fun getMetadataValue(key: String): String?
    suspend fun setMetadataValue(key: String, value: String)
    suspend fun deleteMetadataKey(key: String)

    // Maintenance
    suspend fun clearAllData()
    suspend fun getAllSentRequestsSnapshot(): List<SentRequest>
}
