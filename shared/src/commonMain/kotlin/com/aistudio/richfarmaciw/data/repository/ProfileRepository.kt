package com.aistudio.richfarmaciw.data.repository

import com.aistudio.richfarmaciw.data.db.ProfileDao
import com.aistudio.richfarmaciw.data.model.Profile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {
    val allProfiles: Flow<List<Profile>> = profileDao.getAllProfiles()

    suspend fun getProfileById(id: String): Profile? = profileDao.getProfileById(id)

    suspend fun insertProfile(profile: Profile) = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: Profile) = profileDao.deleteProfile(profile)
}
