package com.aistudio.richfarmaciw.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun createDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".richfarmaci_database.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}

private var dbInstance: RichFarmaciDatabase? = null

actual fun provideRichFarmaciDatabase(context: Any?): RichFarmaciDatabase {
    return dbInstance ?: getRoomDatabase(createDatabaseBuilder(context)).also { dbInstance = it }
}
