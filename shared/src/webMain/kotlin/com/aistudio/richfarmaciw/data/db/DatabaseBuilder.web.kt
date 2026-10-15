package com.aistudio.richfarmaciw.data.db

private var webDbInstance: RichFarmaciDatabase? = null

actual fun provideRichFarmaciDatabase(context: Any?): RichFarmaciDatabase {
    return webDbInstance ?: WebRichFarmaciDatabase().also { webDbInstance = it }
}
