package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.model.Medication

actual object PlatformNotificationHelper {
    actual fun scheduleNotification(context: Any?, medication: Medication, description: String) {
        println("Web Notification Scheduled: ${medication.nome} - $description")
    }

    actual fun cancelNotification(context: Any?, medication: Medication) {
        println("Web Notification Cancelled: ${medication.nome}")
    }

    actual fun updateAllNotifications(context: Any?, medications: List<Medication>, notificationsEnabled: Boolean, description: String) {
        println("Web Notifications Updated: enabled=$notificationsEnabled")
    }

    actual fun showNotification(context: Any?, title: String, message: String) {
        println("Browser Alert: [$title] $message")
    }

    actual fun sendTestNotification(context: Any?) {
        showNotification(context, "RichFarmaci Web Test", "Le notifiche nel browser sono in fase di sviluppo.")
    }
}
