package com.aistudio.richfarmaciw.data.db

import androidx.room.*
import com.aistudio.richfarmaciw.data.model.AppMetadata

@Dao
interface AppMetadataDao {
    @Query("SELECT value FROM app_metadata WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValue(metadata: AppMetadata)

    @Query("DELETE FROM app_metadata WHERE `key` = :key")
    suspend fun deleteKey(key: String)

    @Query("DELETE FROM app_metadata")
    suspend fun deleteAll()
}
