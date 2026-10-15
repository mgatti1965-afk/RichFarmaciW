package com.aistudio.richfarmaciw.data.repository

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.SentRequest
import kotlinx.coroutines.flow.Flow

class SentRequestRepository(private val db: RichFarmaciDatabase) {
    fun getSentRequestsByProfile(profileId: String): Flow<List<SentRequest>> =
        db.getSentRequestsByProfile(profileId)

    suspend fun insert(sentRequest: SentRequest) {
        db.insertSentRequest(sentRequest)
    }

    suspend fun delete(sentRequest: SentRequest) {
        db.deleteSentRequest(sentRequest)
    }
}
