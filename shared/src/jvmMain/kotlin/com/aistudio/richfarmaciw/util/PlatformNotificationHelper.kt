package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.model.Medication

actual object PlatformNotificationHelper {
    actual fun scheduleNotification(context: Any?, medication: Medication, description: String) {
        // Desktop implementation placeholder (can use java.awt.SystemTray or OS scripts if desired)
    }

    actual fun cancelNotification(context: Any?, medication: Medication) {
        // Desktop implementation placeholder
    }

    actual fun updateAllNotifications(context: Any?, medications: List<Medication>, notificationsEnabled: Boolean, description: String) {
        // Desktop implementation placeholder
    }

    actual fun showNotification(context: Any?, title: String, message: String) {
        println("System Notification: [$title] - $message")
    }

    actual fun sendTestNotification(context: Any?) {
        showNotification(context, "RichFarmaci Desktop Test", "Le notifiche desktop funzionano!")
    }
}
