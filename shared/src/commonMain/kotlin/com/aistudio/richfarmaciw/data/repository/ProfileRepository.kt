package com.aistudio.richfarmaciw.data.repository

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.Profile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val db: RichFarmaciDatabase) {
    val allProfiles: Flow<List<Profile>> = db.getAllProfiles()

    suspend fun getProfileById(id: String): Profile? = db.getProfileById(id)

    suspend fun insertProfile(profile: Profile) = db.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = db.updateProfile(profile)

    suspend fun deleteProfile(profile: Profile) = db.deleteProfile(profile)
}
