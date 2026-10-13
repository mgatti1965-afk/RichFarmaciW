package com.aistudio.richfarmaciw.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun createDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val ctx = context as? Context
    val appContext = ctx?.applicationContext ?: ctx
    val dbFile = try {
        appContext?.getDatabasePath("richfarmaci_database.db")
    } catch (_: Throwable) {
        null
    }
    val dbPath = dbFile?.absolutePath ?: "richfarmaci_database.db"

    return if (appContext != null) {
        Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbPath
        )
    } else {
        throw IllegalArgumentException("Context must be provided for Android database builder")
    }
}



