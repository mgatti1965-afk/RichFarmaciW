package com.aistudio.richfarmaciw.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aistudio.richfarmaciw.data.db.AppDatabase
import com.aistudio.richfarmaciw.data.db.provideRichFarmaciDatabase
import com.aistudio.richfarmaciw.util.PlatformNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAlarms(context)
            return
        }

        val medicationId = intent.getStringExtra("medication_id") ?: return
        val medicationName = intent.getStringExtra("medication_name") ?: "Farmaco"
        val description = intent.getStringExtra("description") ?: "È ora di prendere il farmaco"

        val db = provideRichFarmaciDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val medications = db.getAllMedicationsSnapshot()
            val medication = medications.find { it.id == medicationId }

            if (medication != null) {
                val profile = db.getProfileById(medication.profileId)
                if (profile?.notificheAttive == true) {
                    PlatformNotificationHelper.showNotification(context, medicationName, description)
                }

                if (medication.notificaAttiva && !medication.inPausa && profile?.notificheAttive == true) {
                    val nextDescription = profile.descrizioneNotifica
                    PlatformNotificationHelper.scheduleNotification(context, medication, nextDescription)
                }
            }
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        val db = provideRichFarmaciDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val medications = db.getAllMedicationsSnapshot()
            medications.filter { it.notificaAttiva && !it.inPausa }.forEach { med ->
                val profile = db.getProfileById(med.profileId)
                if (profile?.notificheAttive == true) {
                    val description = profile.descrizioneNotifica
                    PlatformNotificationHelper.scheduleNotification(context, med, description)
                }
            }
        }
    }
}
