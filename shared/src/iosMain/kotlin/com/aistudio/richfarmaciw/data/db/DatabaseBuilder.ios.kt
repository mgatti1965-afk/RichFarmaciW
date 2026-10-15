package com.aistudio.richfarmaciw.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun createDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/richfarmaci_database.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = { AppDatabaseConstructor.initialize() }
    )
}

private var dbInstance: RichFarmaciDatabase? = null

actual fun provideRichFarmaciDatabase(context: Any?): RichFarmaciDatabase {
    return dbInstance ?: getRoomDatabase(createDatabaseBuilder(context)).also { dbInstance = it }
}
