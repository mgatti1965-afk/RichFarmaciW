package com.aistudio.richfarmaciw.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.booleanOrNull

fun generateId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..16).map { chars.random() }.joinToString("")
}

@Serializable(with = AppMetadataSerializer::class)
data class AppMetadata(
    val key: String,
    val value: String
)

object AppMetadataSerializer : KSerializer<AppMetadata> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AppMetadata", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: AppMetadata) {
        encoder.encodeString("${value.key}:::${value.value}")
    }
    override fun deserialize(decoder: Decoder): AppMetadata {
        val parts = decoder.decodeString().split(":::")
        return AppMetadata(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
    }
}

@Serializable(with = ProfileSerializer::class)
data class Profile(
    val id: String = generateId(),
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

object ProfileSerializer : KSerializer<Profile> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Profile", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Profile) {
        val json = buildJsonObject {
            put("id", JsonPrimitive(value.id))
            put("pazienteNome", kotlinx.serialization.json.JsonPrimitive(value.pazienteNome))
            put("pazienteCf", kotlinx.serialization.json.JsonPrimitive(value.pazienteCf))
            put("medicoNome", kotlinx.serialization.json.JsonPrimitive(value.medicoNome))
            put("medicoTelefono", kotlinx.serialization.json.JsonPrimitive(value.medicoTelefono))
            put("medicoEmail", kotlinx.serialization.json.JsonPrimitive(value.medicoEmail))
            put("secondoIndirizzo", kotlinx.serialization.json.JsonPrimitive(value.secondoIndirizzo))
            put("messaggioTesta", kotlinx.serialization.json.JsonPrimitive(value.messaggioTesta))
            put("messaggioCoda", kotlinx.serialization.json.JsonPrimitive(value.messaggioCoda))
            put("tipoInvio", kotlinx.serialization.json.JsonPrimitive(value.tipoInvio))
            put("notificheAttive", kotlinx.serialization.json.JsonPrimitive(value.notificheAttive))
            put("descrizioneNotifica", kotlinx.serialization.json.JsonPrimitive(value.descrizioneNotifica))
        }
        encoder.encodeString(json.toString())
    }
    override fun deserialize(decoder: Decoder): Profile {
        val obj = Json.parseToJsonElement(decoder.decodeString()).jsonObject
        return Profile(
            id = obj["id"]?.jsonPrimitive?.content ?: generateId(),
            pazienteNome = obj["pazienteNome"]?.jsonPrimitive?.content ?: "",
            pazienteCf = obj["pazienteCf"]?.jsonPrimitive?.content ?: "",
            medicoNome = obj["medicoNome"]?.jsonPrimitive?.content ?: "",
            medicoTelefono = obj["medicoTelefono"]?.jsonPrimitive?.content ?: "",
            medicoEmail = obj["medicoEmail"]?.jsonPrimitive?.content ?: "",
            secondoIndirizzo = obj["secondoIndirizzo"]?.jsonPrimitive?.content ?: "",
            messaggioTesta = obj["messaggioTesta"]?.jsonPrimitive?.content ?: "Gentile Dottore, Le chiedo cortesemente la prescrizione dei seguenti medicinali intestati a me:",
            messaggioCoda = obj["messaggioCoda"]?.jsonPrimitive?.content ?: "La ringrazio per la disponibilità. Cordiali saluti.",
            tipoInvio = obj["tipoInvio"]?.jsonPrimitive?.intOrNull ?: 0,
            notificheAttive = obj["notificheAttive"]?.jsonPrimitive?.booleanOrNull ?: false,
            descrizioneNotifica = obj["descrizioneNotifica"]?.jsonPrimitive?.content ?: "È ora di prendere il farmaco"
        )
    }
}

@Serializable(with = MedicationSerializer::class)
data class Medication(
    val id: String = generateId(),
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

object MedicationSerializer : KSerializer<Medication> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Medication", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Medication) {
        val json = buildJsonObject {
            put("id", kotlinx.serialization.json.JsonPrimitive(value.id))
            put("profileId", kotlinx.serialization.json.JsonPrimitive(value.profileId))
            put("nome", kotlinx.serialization.json.JsonPrimitive(value.nome))
            put("scatole", kotlinx.serialization.json.JsonPrimitive(value.scatole))
            put("note", kotlinx.serialization.json.JsonPrimitive(value.note))
            put("inPausa", kotlinx.serialization.json.JsonPrimitive(value.inPausa))
            put("notificaAttiva", kotlinx.serialization.json.JsonPrimitive(value.notificaAttiva))
            put("orarioNotifica", kotlinx.serialization.json.JsonPrimitive(value.orarioNotifica))
            put("frequenzaValore", kotlinx.serialization.json.JsonPrimitive(value.frequenzaValore))
            put("frequenzaTipo", kotlinx.serialization.json.JsonPrimitive(value.frequenzaTipo))
        }
        encoder.encodeString(json.toString())
    }
    override fun deserialize(decoder: Decoder): Medication {
        val obj = Json.parseToJsonElement(decoder.decodeString()).jsonObject
        return Medication(
            id = obj["id"]?.jsonPrimitive?.content ?: generateId(),
            profileId = obj["profileId"]?.jsonPrimitive?.content ?: "",
            nome = obj["nome"]?.jsonPrimitive?.content ?: "",
            scatole = obj["scatole"]?.jsonPrimitive?.intOrNull ?: 1,
            note = obj["note"]?.jsonPrimitive?.content ?: "",
            inPausa = obj["inPausa"]?.jsonPrimitive?.booleanOrNull ?: false,
            notificaAttiva = obj["notificaAttiva"]?.jsonPrimitive?.booleanOrNull ?: false,
            orarioNotifica = obj["orarioNotifica"]?.jsonPrimitive?.content ?: "08:00",
            frequenzaValore = obj["frequenzaValore"]?.jsonPrimitive?.intOrNull ?: 1,
            frequenzaTipo = obj["frequenzaTipo"]?.jsonPrimitive?.content ?: "GIORNI"
        )
    }
}

@Serializable(with = SentMedicationSerializer::class)
data class SentMedication(
    val nome: String,
    val scatole: Int,
    val note: String = ""
)

object SentMedicationSerializer : KSerializer<SentMedication> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SentMedication", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SentMedication) {
        val json = buildJsonObject {
            put("nome", kotlinx.serialization.json.JsonPrimitive(value.nome))
            put("scatole", kotlinx.serialization.json.JsonPrimitive(value.scatole))
            put("note", kotlinx.serialization.json.JsonPrimitive(value.note))
        }
        encoder.encodeString(json.toString())
    }
    override fun deserialize(decoder: Decoder): SentMedication {
        val obj = Json.parseToJsonElement(decoder.decodeString()).jsonObject
        return SentMedication(
            nome = obj["nome"]?.jsonPrimitive?.content ?: "",
            scatole = obj["scatole"]?.jsonPrimitive?.intOrNull ?: 1,
            note = obj["note"]?.jsonPrimitive?.content ?: ""
        )
    }
}

@Serializable(with = SentRequestSerializer::class)
data class SentRequest(
    val id: String = generateId(),
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

object SentRequestSerializer : KSerializer<SentRequest> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SentRequest", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SentRequest) {
        val json = buildJsonObject {
            put("id", kotlinx.serialization.json.JsonPrimitive(value.id))
            put("profileId", kotlinx.serialization.json.JsonPrimitive(value.profileId))
            put("data", kotlinx.serialization.json.JsonPrimitive(value.data))
            put("pazienteNome", kotlinx.serialization.json.JsonPrimitive(value.pazienteNome))
            put("medicoNome", kotlinx.serialization.json.JsonPrimitive(value.medicoNome))
            put("farmaciSerialized", kotlinx.serialization.json.JsonPrimitive(value.farmaciSerialized))
            put("testoCompleto", kotlinx.serialization.json.JsonPrimitive(value.testoCompleto))
        }
        encoder.encodeString(json.toString())
    }
    override fun deserialize(decoder: Decoder): SentRequest {
        val obj = Json.parseToJsonElement(decoder.decodeString()).jsonObject
        return SentRequest(
            id = obj["id"]?.jsonPrimitive?.content ?: generateId(),
            profileId = obj["profileId"]?.jsonPrimitive?.content ?: "",
            data = obj["data"]?.jsonPrimitive?.content ?: "",
            pazienteNome = obj["pazienteNome"]?.jsonPrimitive?.content ?: "",
            medicoNome = obj["medicoNome"]?.jsonPrimitive?.content ?: "",
            farmaciSerialized = obj["farmaciSerialized"]?.jsonPrimitive?.content ?: "",
            testoCompleto = obj["testoCompleto"]?.jsonPrimitive?.content ?: ""
        )
    }
}

@Serializable(with = BackupDataSerializer::class)
data class BackupData(
    val version: Int = 1,
    val profiles: List<Profile> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val sentRequests: List<SentRequest> = emptyList(),
    val donationCount: Int = 0,
    val onboardingShown: Boolean = true,
    val disclaimerAccepted: Boolean = true
)

object BackupDataSerializer : KSerializer<BackupData> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BackupData", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BackupData) {
        val json = buildJsonObject {
            put("version", kotlinx.serialization.json.JsonPrimitive(value.version))
            put("donationCount", kotlinx.serialization.json.JsonPrimitive(value.donationCount))
            put("onboardingShown", kotlinx.serialization.json.JsonPrimitive(value.onboardingShown))
            put("disclaimerAccepted", kotlinx.serialization.json.JsonPrimitive(value.disclaimerAccepted))
        }
        encoder.encodeString(json.toString())
    }
    override fun deserialize(decoder: Decoder): BackupData {
        val obj = Json.parseToJsonElement(decoder.decodeString()).jsonObject
        return BackupData(
            version = obj["version"]?.jsonPrimitive?.intOrNull ?: 1,
            donationCount = obj["donationCount"]?.jsonPrimitive?.intOrNull ?: 0,
            onboardingShown = obj["onboardingShown"]?.jsonPrimitive?.booleanOrNull ?: true,
            disclaimerAccepted = obj["disclaimerAccepted"]?.jsonPrimitive?.booleanOrNull ?: true
        )
    }
}

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
