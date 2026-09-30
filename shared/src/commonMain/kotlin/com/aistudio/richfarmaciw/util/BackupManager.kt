package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.BackupData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupManager(private val db: RichFarmaciDatabase) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun createBackupJson(): String {
        val profiles = db.getAllProfilesSnapshot()
        val medications = db.getAllMedicationsSnapshot()
        val sentRequests = db.getAllSentRequestsSnapshot()

        val onboardingShown = db.getMetadataValue("onboarding_shown") == "true"
        val disclaimerAccepted = db.getMetadataValue("disclaimer_accepted") == "true"
        val donationCount = db.getMetadataValue("donation_count")?.toIntOrNull() ?: 0

        val backup = BackupData(
            version = 1,
            profiles = profiles,
            medications = medications,
            sentRequests = sentRequests,
            donationCount = donationCount,
            onboardingShown = onboardingShown,
            disclaimerAccepted = disclaimerAccepted
        )

        return json.encodeToString(backup)
    }

    suspend fun restoreFromJson(jsonString: String): Result<Unit> {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)

            // Per semplicità facciamo una pulizia e reinserimento
            // In un caso reale potremmo voler fare un merge intelligente
            db.clearAllData()

            backup.profiles.forEach { db.insertProfile(it) }
            backup.medications.forEach { db.insertMedication(it) }
            backup.sentRequests.forEach { db.insertSentRequest(it) }

            db.setMetadataValue("onboarding_shown", backup.onboardingShown.toString())
            db.setMetadataValue("disclaimer_accepted", backup.disclaimerAccepted.toString())
            db.setMetadataValue("donation_count", backup.donationCount.toString())

            if (backup.profiles.isNotEmpty()) {
                db.setMetadataValue("active_profile_id", backup.profiles.first().id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
