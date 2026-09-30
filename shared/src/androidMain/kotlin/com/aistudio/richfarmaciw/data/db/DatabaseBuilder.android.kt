package com.aistudio.richfarmaciw.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun createDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val appContext = (context as Context).applicationContext
    val dbFile = appContext.getDatabasePath("richfarmaci_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

