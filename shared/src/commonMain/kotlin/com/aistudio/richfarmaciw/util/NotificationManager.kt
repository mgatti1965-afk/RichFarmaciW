package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.model.Medication

expect object PlatformNotificationHelper {
    fun scheduleNotification(context: Any?, medication: Medication, description: String)
    fun cancelNotification(context: Any?, medication: Medication)
    fun updateAllNotifications(context: Any?, medications: List<Medication>, notificationsEnabled: Boolean, description: String)
    fun showNotification(context: Any?, title: String, message: String)
    fun sendTestNotification(context: Any?)
}
