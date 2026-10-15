package com.aistudio.richfarmaciw.data.repository

import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.Medication
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val db: RichFarmaciDatabase) {

    fun getMedicationsByProfile(profileId: String): Flow<List<Medication>> =
        db.getMedicationsByProfile(profileId)

    suspend fun getMedicationsSnapshotByProfile(profileId: String): List<Medication> =
        db.getMedicationsSnapshotByProfile(profileId)

    suspend fun insert(medication: Medication) = db.insertMedication(medication)

    suspend fun update(medication: Medication) = db.updateMedication(medication)

    suspend fun delete(medication: Medication) = db.deleteMedication(medication)

    suspend fun deleteById(id: String) = db.deleteMedicationById(id)

    fun getDefaultMedicationsList(profileId: String) = listOf(
        Medication(profileId = profileId, nome = "Zanedip 10 mg", scatole = 2, note = "compresse"),
        Medication(profileId = profileId, nome = "Lasix 25 mg", scatole = 4, note = "compresse"),
        Medication(profileId = profileId, nome = "Pantoprazolo 20 mg", scatole = 1, note = "protettore stomaco"),
        Medication(profileId = profileId, nome = "Cardioaspirina 100 mg", scatole = 1, note = "dopo pranzo"),
        Medication(profileId = profileId, nome = "Zyloric 100 mg", scatole = 1, note = "compresse"),
        Medication(profileId = profileId, nome = "Tareg 160 mg", scatole = 1, note = "compresse della pressione"),
        Medication(profileId = profileId, nome = "Dibase 50.000 u.i./2,5ml", scatole = 1, note = "gocce orali")
    )

    suspend fun forcePrepopulateWithDefaults(profileId: String) {
        db.insertAllMedications(getDefaultMedicationsList(profileId))
    }
}
