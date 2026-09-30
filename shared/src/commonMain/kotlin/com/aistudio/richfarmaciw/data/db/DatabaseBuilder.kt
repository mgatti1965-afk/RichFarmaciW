package com.aistudio.richfarmaciw.data.db

import androidx.room.RoomDatabase

expect fun createDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<AppDatabase>

private var dbInstance: RichFarmaciDatabase? = null

fun provideRichFarmaciDatabase(context: Any? = null): RichFarmaciDatabase {
    return dbInstance ?: getRoomDatabase(createDatabaseBuilder(context)).also { dbInstance = it }
}

