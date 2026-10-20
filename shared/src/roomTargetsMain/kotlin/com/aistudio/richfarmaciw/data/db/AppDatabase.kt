package com.aistudio.richfarmaciw.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aistudio.richfarmaciw.data.model.Medication
import com.aistudio.richfarmaciw.data.model.Profile
import com.aistudio.richfarmaciw.data.model.SentRequest
import com.aistudio.richfarmaciw.data.model.AppMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [Medication::class, Profile::class, SentRequest::class, AppMetadata::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase(), RichFarmaciDatabase {
    abstract fun medicationDao(): MedicationDao
    abstract fun profileDao(): ProfileDao
    abstract fun sentRequestDao(): SentRequestDao
    abstract fun appMetadataDao(): AppMetadataDao

    override fun getAllProfiles() = profileDao().getAllProfiles()
    override suspend fun getProfileById(id: String) = profileDao().getProfileById(id)
    override suspend fun insertProfile(profile: Profile) = profileDao().insertProfile(profile)
    override suspend fun updateProfile(profile: Profile) = profileDao().updateProfile(profile)
    override suspend fun deleteProfile(profile: Profile) = profileDao().deleteProfile(profile)
    override suspend fun getAllProfilesSnapshot() = profileDao().getAllProfilesSnapshot()

    override fun getMedicationsByProfile(profileId: String) = medicationDao().getMedicationsByProfile(profileId)
    override suspend fun getMedicationsSnapshotByProfile(profileId: String) = medicationDao().getMedicationsSnapshotByProfile(profileId)
    override suspend fun getAllMedicationsSnapshot() = medicationDao().getAllMedicationsSnapshot()
    override suspend fun getMedicationById(id: String) = medicationDao().getMedicationById(id)
    override suspend fun insertMedication(medication: Medication) = medicationDao().insert(medication)
    override suspend fun insertAllMedications(medications: List<Medication>) = medicationDao().insertAll(medications)
    override suspend fun updateMedication(medication: Medication) = medicationDao().update(medication)
    override suspend fun deleteMedication(medication: Medication) = medicationDao().delete(medication)
    override suspend fun deleteMedicationById(id: String) = medicationDao().deleteById(id)

    override fun getSentRequestsByProfile(profileId: String) = sentRequestDao().getSentRequestsByProfile(profileId)
    override suspend fun insertSentRequest(sentRequest: SentRequest) = sentRequestDao().insert(sentRequest)
    override suspend fun deleteSentRequest(sentRequest: SentRequest) = sentRequestDao().delete(sentRequest)

    override suspend fun getMetadataValue(key: String) = appMetadataDao().getValue(key)
    override suspend fun setMetadataValue(key: String, value: String) = appMetadataDao().setValue(AppMetadata(key, value))
    override suspend fun deleteMetadataKey(key: String) = appMetadataDao().deleteKey(key)

    override suspend fun getAllSentRequestsSnapshot(): List<SentRequest> = sentRequestDao().getAllSnapshot()

    override suspend fun clearAllData() {
        medicationDao().deleteAll()
        profileDao().deleteAll()
        sentRequestDao().deleteAll()
        appMetadataDao().deleteAll()
    }
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
