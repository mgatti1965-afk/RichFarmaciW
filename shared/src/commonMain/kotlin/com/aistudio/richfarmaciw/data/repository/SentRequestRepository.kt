package com.aistudio.richfarmaciw.data.repository

import com.aistudio.richfarmaciw.data.db.SentRequestDao
import com.aistudio.richfarmaciw.data.model.SentRequest
import kotlinx.coroutines.flow.Flow

class SentRequestRepository(private val sentRequestDao: SentRequestDao) {
    fun getSentRequestsByProfile(profileId: String): Flow<List<SentRequest>> =
        sentRequestDao.getSentRequestsByProfile(profileId)

    suspend fun insert(sentRequest: SentRequest) {
        sentRequestDao.insert(sentRequest)
    }

    suspend fun delete(sentRequest: SentRequest) {
        sentRequestDao.delete(sentRequest)
    }

    suspend fun deleteById(id: String) {
        sentRequestDao.deleteById(id)
    }

    suspend fun clearAll() {
        sentRequestDao.deleteAll()
    }
}
