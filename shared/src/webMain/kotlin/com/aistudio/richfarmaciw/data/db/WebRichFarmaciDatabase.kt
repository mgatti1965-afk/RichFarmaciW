package com.aistudio.richfarmaciw.data.db

import com.aistudio.richfarmaciw.data.model.*
import com.aistudio.richfarmaciw.util.webGetItem
import com.aistudio.richfarmaciw.util.webRemoveItem
import com.aistudio.richfarmaciw.util.webSetItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebRichFarmaciDatabase : RichFarmaciDatabase {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val KEY_PROFILES = "rfw_profiles"
    private val KEY_MEDICATIONS = "rfw_medications"
    private val KEY_SENT_REQUESTS = "rfw_sent_requests"
    private val KEY_METADATA = "rfw_metadata"

    private val profilesFlow = MutableStateFlow(loadProfiles())
    private val medicationsFlow = MutableStateFlow(loadMedications())
    private val sentRequestsFlow = MutableStateFlow(loadSentRequests())
    private val metadataMap = loadMetadata().toMutableMap()

    private fun loadProfiles(): List<Profile> {
        return try {
            val raw = webGetItem(KEY_PROFILES) ?: return emptyList()
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveProfiles(list: List<Profile>) {
        try {
            webSetItem(KEY_PROFILES, json.encodeToString(list))
        } catch (_: Exception) {}
        profilesFlow.value = list
    }

    private fun loadMedications(): List<Medication> {
        return try {
            val raw = webGetItem(KEY_MEDICATIONS) ?: return emptyList()
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveMedications(list: List<Medication>) {
        try {
            webSetItem(KEY_MEDICATIONS, json.encodeToString(list))
        } catch (_: Exception) {}
        medicationsFlow.value = list
    }

    private fun loadSentRequests(): List<SentRequest> {
        return try {
            val raw = webGetItem(KEY_SENT_REQUESTS) ?: return emptyList()
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveSentRequests(list: List<SentRequest>) {
        try {
            webSetItem(KEY_SENT_REQUESTS, json.encodeToString(list))
        } catch (_: Exception) {}
        sentRequestsFlow.value = list
    }

    private fun loadMetadata(): Map<String, String> {
        return try {
            val raw = webGetItem(KEY_METADATA) ?: return emptyMap()
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun saveMetadata() {
        try {
            webSetItem(KEY_METADATA, json.encodeToString(metadataMap))
        } catch (_: Exception) {}
    }

    // Profiles
    override fun getAllProfiles(): Flow<List<Profile>> = profilesFlow

    override suspend fun getProfileById(id: String): Profile? {
        return profilesFlow.value.find { it.id == id }
    }

    override suspend fun insertProfile(profile: Profile) {
        val current = profilesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            current[index] = profile
        } else {
            current.add(profile)
        }
        saveProfiles(current)
    }

    override suspend fun updateProfile(profile: Profile) {
        insertProfile(profile)
    }

    override suspend fun deleteProfile(profile: Profile) {
        val current = profilesFlow.value.toMutableList()
        current.removeAll { it.id == profile.id }
        saveProfiles(current)
    }

    override suspend fun getAllProfilesSnapshot(): List<Profile> = profilesFlow.value

    // Medications
    override fun getMedicationsByProfile(profileId: String): Flow<List<Medication>> {
        return medicationsFlow.map { list -> list.filter { it.profileId == profileId } }
    }

    override suspend fun getMedicationsSnapshotByProfile(profileId: String): List<Medication> {
        return medicationsFlow.value.filter { it.profileId == profileId }
    }

    override suspend fun getAllMedicationsSnapshot(): List<Medication> = medicationsFlow.value

    override suspend fun getMedicationById(id: String): Medication? {
        return medicationsFlow.value.find { it.id == id }
    }

    override suspend fun insertMedication(medication: Medication) {
        val current = medicationsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == medication.id }
        if (index != -1) {
            current[index] = medication
        } else {
            current.add(medication)
        }
        saveMedications(current)
    }

    override suspend fun insertAllMedications(medications: List<Medication>) {
        val current = medicationsFlow.value.toMutableList()
        medications.forEach { med ->
            val index = current.indexOfFirst { it.id == med.id }
            if (index != -1) {
                current[index] = med
            } else {
                current.add(med)
            }
        }
        saveMedications(current)
    }

    override suspend fun updateMedication(medication: Medication) {
        insertMedication(medication)
    }

    override suspend fun deleteMedication(medication: Medication) {
        deleteMedicationById(medication.id)
    }

    override suspend fun deleteMedicationById(id: String) {
        val current = medicationsFlow.value.toMutableList()
        current.removeAll { it.id == id }
        saveMedications(current)
    }

    // Sent Requests
    override fun getSentRequestsByProfile(profileId: String): Flow<List<SentRequest>> {
        return sentRequestsFlow.map { list -> list.filter { it.profileId == profileId } }
    }

    override suspend fun insertSentRequest(sentRequest: SentRequest) {
        val current = sentRequestsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == sentRequest.id }
        if (index != -1) {
            current[index] = sentRequest
        } else {
            current.add(0, sentRequest)
        }
        saveSentRequests(current)
    }

    override suspend fun deleteSentRequest(sentRequest: SentRequest) {
        val current = sentRequestsFlow.value.toMutableList()
        current.removeAll { it.id == sentRequest.id }
        saveSentRequests(current)
    }

    override suspend fun getAllSentRequestsSnapshot(): List<SentRequest> = sentRequestsFlow.value

    // Metadata
    override suspend fun getMetadataValue(key: String): String? = metadataMap[key]

    override suspend fun setMetadataValue(key: String, value: String) {
        metadataMap[key] = value
        saveMetadata()
    }

    override suspend fun deleteMetadataKey(key: String) {
        metadataMap.remove(key)
        saveMetadata()
    }

    // Maintenance
    override suspend fun clearAllData() {
        try {
            webRemoveItem(KEY_PROFILES)
            webRemoveItem(KEY_MEDICATIONS)
            webRemoveItem(KEY_SENT_REQUESTS)
            webRemoveItem(KEY_METADATA)
        } catch (_: Exception) {}
        profilesFlow.value = emptyList()
        medicationsFlow.value = emptyList()
        sentRequestsFlow.value = emptyList()
        metadataMap.clear()
    }
}
