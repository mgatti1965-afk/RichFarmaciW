package com.aistudio.richfarmaciw.util

import com.aistudio.richfarmaciw.data.model.Medication
import platform.UserNotifications.*
import platform.Foundation.*

actual object PlatformNotificationHelper {
    actual fun scheduleNotification(context: Any?, medication: Medication, description: String) {
        if (!medication.notificaAttiva || medication.inPausa) {
            cancelNotification(context, medication)
            return
        }

        val center = UNUserNotificationCenter.currentNotificationCenter()

        val content = UNMutableNotificationContent().apply {
            setTitle(medication.nome)
            setBody(description)
            setSound(UNNotificationSound.defaultSound())
        }

        val orari = medication.orarioNotifica.split(",").filter { it.isNotBlank() }
        if (orari.isEmpty()) return

        orari.forEachIndexed { index, orario ->
            val parts = orario.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 8
                val minute = parts[1].toIntOrNull() ?: 0

                val content = UNMutableNotificationContent().apply {
                    setTitle(medication.nome)
                    setBody(description)
                    setSound(UNNotificationSound.defaultSound())
                }

                val dateComponents = NSDateComponents().apply {
                    setHour(hour.toLong())
                    setMinute(minute.toLong())
                }

                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents, repeats = true)
                val identifier = "${medication.id}_$index"
                val request = UNNotificationRequest.requestWithIdentifier(identifier, content, trigger)

                center.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("Error scheduling notification $identifier: ${error.localizedDescription}")
                    }
                }
            }
        }
    }

    actual fun cancelNotification(context: Any?, medication: Medication) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        // Cancels all potential slots (assuming max 20 slots for safety)
        val identifiers = (0..20).map { "${medication.id}_$it" }
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
    }

    actual fun updateAllNotifications(context: Any?, medications: List<Medication>, notificationsEnabled: Boolean, description: String) {
        if (!notificationsEnabled) {
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.removeAllPendingNotificationRequests()
        } else {
            medications.forEach { scheduleNotification(context, it, description) }
        }
    }

    actual fun showNotification(context: Any?, title: String, message: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            NSUUID().UUIDString(),
            content,
            trigger = null
        )
        center.addNotificationRequest(request, null)
    }

    actual fun sendTestNotification(context: Any?) {
        showNotification(context, "RichFarmaci - Test", "Le notifiche su iOS sono configurate!")
    }
}
