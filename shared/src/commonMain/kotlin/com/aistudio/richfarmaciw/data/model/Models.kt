package com.aistudio.richfarmaciw.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

fun generateId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..16).map { chars.random() }.joinToString("")
}

@Entity(tableName = "app_metadata")
@Serializable
data class AppMetadata(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "profiles")
@Serializable
data class Profile(
    @PrimaryKey val id: String = generateId(),
    val pazienteNome: String = "",
    val pazienteCf: String = "",
    val medicoNome: String = "",
    val medicoTelefono: String = "",
    val medicoEmail: String = "",
    val secondoIndirizzo: String = "",
    val messaggioTesta: String = "Gentile Dottore, Le chiedo cortesemente la prescrizione dei seguenti medicinali intestati a me:",
    val messaggioCoda: String = "La ringrazio per la disponibilità. Cordiali saluti.",
    val tipoInvio: Int = 0,
    val notificheAttive: Boolean = false,
    val descrizioneNotifica: String = "È ora di prendere il farmaco"
)

@Entity(tableName = "medications")
@Serializable
data class Medication(
    @PrimaryKey val id: String = generateId(),
    val profileId: String,
    val nome: String,
    val scatole: Int = 1,
    val note: String = "",
    val inPausa: Boolean = false,
    val notificaAttiva: Boolean = false,
    val orarioNotifica: String = "08:00",
    val frequenzaValore: Int = 1,
    val frequenzaTipo: String = "GIORNI"
)

@Serializable
data class SentMedication(
    val nome: String,
    val scatole: Int,
    val note: String = ""
)

@Entity(tableName = "sent_requests")
@Serializable
data class SentRequest(
    @PrimaryKey val id: String = generateId(),
    val profileId: String,
    val data: String,
    val pazienteNome: String,
    val medicoNome: String,
    val farmaciSerialized: String,
    val testoCompleto: String
) {
    fun getSentMedications(): List<SentMedication> {
        return deserializeMedicines(farmaciSerialized)
    }

    companion object {
        fun serializeMedicines(list: List<SentMedication>): String {
            return list.joinToString("|||") {
                "${escape(it.nome)}###${it.scatole}###${escape(it.note)}"
            }
        }

        fun deserializeMedicines(str: String): List<SentMedication> {
            if (str.isBlank()) return emptyList()
            return str.split("|||").mapNotNull { part ->
                val subparts = part.split("###")
                if (subparts.size >= 2) {
                    val nome = unescape(subparts[0])
                    val scatole = subparts[1].toIntOrNull() ?: 1
                    val note = if (subparts.size > 2) unescape(subparts[2]) else ""
                    SentMedication(nome, scatole, note)
                } else {
                    null
                }
            }
        }

        private fun escape(s: String): String = s.replace("|", "\\bar").replace("#", "\\hash")
        private fun unescape(s: String): String = s.replace("\\bar", "|").replace("\\hash", "#")
    }
}

@Serializable
data class BackupData(
    val version: Int = 1,
    val profiles: List<Profile> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val sentRequests: List<SentRequest> = emptyList(),
    val donationCount: Int = 0,
    val onboardingShown: Boolean = true,
    val disclaimerAccepted: Boolean = true
)

data class PatientSettings(
    val pazienteNome: String = "",
    val pazienteCf: String = "",
    val medicoNome: String = "",
    val medicoTelefono: String = "",
    val medicoEmail: String = "",
    val secondoIndirizzo: String = "",
    val messaggioTesta: String = "Gentile Dottore, Le chiedo cortesemente la prescrizione dei seguenti medicinali intestati a me:",
    val messaggioCoda: String = "La ringrazio per la disponibilità. Cordiali saluti.",
    val tipoInvio: Int = 0,
    val notificheAttive: Boolean = false,
    val descrizioneNotifica: String = "È ora di prendere il farmaco"
)
