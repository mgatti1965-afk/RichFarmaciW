package com.aistudio.richfarmaciw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.richfarmaciw.data.db.RichFarmaciDatabase
import com.aistudio.richfarmaciw.data.model.Medication
import com.aistudio.richfarmaciw.data.model.PatientSettings
import com.aistudio.richfarmaciw.data.model.Profile
import com.aistudio.richfarmaciw.data.model.SentMedication
import com.aistudio.richfarmaciw.data.model.SentRequest
import com.aistudio.richfarmaciw.data.model.AppMetadata
import com.aistudio.richfarmaciw.data.model.AccountState
import com.aistudio.richfarmaciw.data.repository.AccountRepository
import com.aistudio.richfarmaciw.util.CloudSyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val db: RichFarmaciDatabase) : ViewModel() {

    private val backupManager = com.aistudio.richfarmaciw.util.BackupManager(db)
    val accountRepository = AccountRepository(db)
    val cloudSyncManager = CloudSyncManager(db, accountRepository)

    val accountState: StateFlow<AccountState> = accountRepository.accountState
    val isSyncing: StateFlow<Boolean> = cloudSyncManager.isSyncing

    // Profiles List
    val profiles: StateFlow<List<Profile>> = db.getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active Profile State
    private val _activeProfileId = MutableStateFlow<String?>(null)
    val activeProfileId: StateFlow<String?> = _activeProfileId.asStateFlow()

    private val _activeProfile = MutableStateFlow<Profile?>(null)
    val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()

    // Screen navigation state
    private val _currentTab = MutableStateFlow(0) // 0 = Request, 1 = History
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Settings State
    private val _settings = MutableStateFlow(PatientSettings())
    val settings: StateFlow<PatientSettings> = _settings.asStateFlow()

    // Configuration Validity
    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    // Medications Flow
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val medications: StateFlow<List<Medication>> = _activeProfileId
        .flatMapLatest { id ->
            if (id != null) db.getMedicationsByProfile(id)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // History Flow
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sentRequests: StateFlow<List<SentRequest>> = _activeProfileId
        .flatMapLatest { id ->
            if (id != null) {
                db.getSentRequestsByProfile(id)
                    .map { list -> list.reversed() }
            } else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _donationCount = MutableStateFlow(0)
    val donationCount: StateFlow<Int> = _donationCount.asStateFlow()

    private val _onboardingShown = MutableStateFlow(false)
    val onboardingShown: StateFlow<Boolean> = _onboardingShown.asStateFlow()

    private val _disclaimerAccepted = MutableStateFlow(false)
    val disclaimerAccepted: StateFlow<Boolean> = _disclaimerAccepted.asStateFlow()

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    private val _showAutoRestorePrompt = MutableStateFlow(false)
    val showAutoRestorePrompt: StateFlow<Boolean> = _showAutoRestorePrompt.asStateFlow()

    // User selection states for the active request form
    private val _selectedMedicationIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMedicationIds: StateFlow<Set<String>> = _selectedMedicationIds.asStateFlow()

    // Map of medication ID -> requested boxes
    private val _selectedQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedQuantities: StateFlow<Map<String, Int>> = _selectedQuantities.asStateFlow()

    // Map of medication ID -> customized notes
    private val _selectedNotes = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedNotes: StateFlow<Map<String, String>> = _selectedNotes.asStateFlow()

    init {
        viewModelScope.launch {
            accountRepository.loadAccountState()
            _onboardingShown.value = db.getMetadataValue("onboarding_shown") == "true"
            _disclaimerAccepted.value = db.getMetadataValue("disclaimer_accepted") == "true"
            _donationCount.value = db.getMetadataValue("donation_count")?.toIntOrNull() ?: 0

            val activeId = db.getMetadataValue("active_profile_id")
            val currentProfiles = db.getAllProfilesSnapshot()

            if (currentProfiles.isNotEmpty()) {
                val targetId = activeId ?: currentProfiles.first().id
                selectProfile(targetId)
            } else {
                _showSettings.value = true
            }
            updateConfigStatus()

            if (accountRepository.accountState.value.isConnected) {
                cloudSyncManager.syncNow()
                val updatedProfiles = db.getAllProfilesSnapshot()
                if (updatedProfiles.isNotEmpty()) {
                    val targetId = db.getMetadataValue("active_profile_id") ?: updatedProfiles.first().id
                    selectProfile(targetId)
                }
            }
        }
    }

    fun createNewAccount() {
        viewModelScope.launch {
            accountRepository.createNewAccount()
            cloudSyncManager.syncNow()
        }
    }

    fun joinAccount(code: String) {
        viewModelScope.launch {
            accountRepository.joinAccount(code)
            cloudSyncManager.syncNow()
            val currentProfiles = db.getAllProfilesSnapshot()
            if (currentProfiles.isNotEmpty()) {
                selectProfile(currentProfiles.first().id)
            }
        }
    }

    fun disconnectAccount() {
        viewModelScope.launch {
            accountRepository.disconnectAccount()
        }
    }

    fun syncCloudNow() {
        viewModelScope.launch {
            cloudSyncManager.syncNow()
            refreshActiveProfile()
            updateConfigStatus()
        }
    }

    private fun triggerAutoSync() {
        // Sincronizzazione cloud disattivata temporaneamente
    }

    fun setOnboardingShown(shown: Boolean) {
        viewModelScope.launch {
            db.setMetadataValue("onboarding_shown", shown.toString())
            _onboardingShown.value = shown
        }
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        viewModelScope.launch {
            db.setMetadataValue("disclaimer_accepted", accepted.toString())
            _disclaimerAccepted.value = accepted
        }
    }

    fun incrementDonationCount() {
        viewModelScope.launch {
            val current = _donationCount.value
            if (current < 1) {
                val next = current + 1
                db.setMetadataValue("donation_count", next.toString())
                _donationCount.value = next
            }
        }
    }

    fun selectProfile(id: String) {
        viewModelScope.launch {
            _activeProfileId.value = id
            db.setMetadataValue("active_profile_id", id)
            refreshActiveProfile()
            clearFormSelection()
            updateConfigStatus()
        }
    }

    private suspend fun refreshActiveProfile() {
        val id = _activeProfileId.value
        if (id != null) {
            _activeProfile.value = db.getProfileById(id)
        } else {
            _activeProfile.value = null
        }
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
        if (!show && _activeProfileId.value == null) {
            viewModelScope.launch {
                val currentProfiles = db.getAllProfilesSnapshot()
                if (currentProfiles.isNotEmpty()) {
                    selectProfile(currentProfiles.first().id)
                } else {
                    _showSettings.value = true
                }
            }
        }
    }

    private fun updateConfigStatus() {
        _isConfigured.value = _activeProfile.value != null
        _settings.value = _activeProfile.value?.let { p ->
            PatientSettings(
                pazienteNome = p.pazienteNome,
                pazienteCf = p.pazienteCf,
                medicoNome = p.medicoNome,
                medicoTelefono = p.medicoTelefono,
                medicoEmail = p.medicoEmail,
                secondoIndirizzo = p.secondoIndirizzo,
                messaggioTesta = p.messaggioTesta,
                messaggioCoda = p.messaggioCoda,
                tipoInvio = p.tipoInvio,
                notificheAttive = p.notificheAttive,
                descrizioneNotifica = p.descrizioneNotifica
            )
        } ?: PatientSettings()
    }

    fun updateSelection(medication: Medication, quantity: Int) {
        val currentSelected = _selectedMedicationIds.value
        if (quantity <= 0) {
            if (currentSelected.contains(medication.id)) {
                toggleMedicationSelection(medication)
            }
        } else {
            if (!currentSelected.contains(medication.id)) {
                toggleMedicationSelection(medication)
            }
            setQuantityForMedication(medication.id, quantity)
        }
    }

    fun getRequestUriString(): String {
        val currentSettings = _settings.value
        val message = buildFormattedMessage()
        return when (currentSettings.tipoInvio) {
            0 -> { // WhatsApp
                val phone = currentSettings.medicoTelefono.replace("+", "").replace(" ", "")
                "https://api.whatsapp.com/send?phone=$phone&text=${encodeUrlQueryComponent(message)}"
            }
            1 -> { // SMS
                "sms:${currentSettings.medicoTelefono}?body=${encodeUrlQueryComponent(message)}"
            }
            2 -> { // Email
                val subject = "Comunicazione da: ${currentSettings.pazienteNome}"
                "mailto:${currentSettings.medicoEmail}?subject=${encodeUrlQueryComponent(subject)}&body=${encodeUrlQueryComponent(message)}"
            }
            else -> ""
        }
    }

    fun getQuickMessageUriString(customText: String): String {
        val fullMessage = buildQuickMessage(customText)
        val currentSettings = _settings.value
        return when (currentSettings.tipoInvio) {
            0 -> { // WhatsApp
                val phone = currentSettings.medicoTelefono.replace("+", "").replace(" ", "")
                "https://api.whatsapp.com/send?phone=$phone&text=${encodeUrlQueryComponent(fullMessage)}"
            }
            1 -> { // SMS
                "sms:${currentSettings.medicoTelefono}?body=${encodeUrlQueryComponent(fullMessage)}"
            }
            2 -> { // Email
                val subject = "Comunicazione da: ${currentSettings.pazienteNome}"
                "mailto:${currentSettings.medicoEmail}?subject=${encodeUrlQueryComponent(subject)}&body=${encodeUrlQueryComponent(fullMessage)}"
            }
            else -> ""
        }
    }

    fun buildQuickMessage(customText: String): String {
        val currentSettings = _settings.value
        return "Messaggio da ${currentSettings.pazienteNome} (CF: ${currentSettings.pazienteCf}):\n\n$customText"
    }

    private fun encodeUrlQueryComponent(s: String): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"
        return s.map { c ->
            if (c in chars) c.toString()
            else if (c == ' ') "+"
            else {
                val code = c.code
                if (code < 256) "%" + code.toString(16).uppercase().padStart(2, '0')
                else c.toString()
            }
        }.joinToString("")
    }

    fun toggleMedicationSelection(medication: Medication) {
        val currentSelected = _selectedMedicationIds.value.toMutableSet()
        if (currentSelected.contains(medication.id)) {
            currentSelected.remove(medication.id)
            val currentQuants = _selectedQuantities.value.toMutableMap()
            currentQuants.remove(medication.id)
            _selectedQuantities.value = currentQuants

            val currentNotes = _selectedNotes.value.toMutableMap()
            currentNotes.remove(medication.id)
            _selectedNotes.value = currentNotes
        } else {
            currentSelected.add(medication.id)
            setQuantityForMedication(medication.id, medication.scatole)
            setNoteForMedication(medication.id, "")
        }
        _selectedMedicationIds.value = currentSelected
    }

    fun setQuantityForMedication(medicationId: String, qty: Int) {
        val currentQuants = _selectedQuantities.value.toMutableMap()
        currentQuants[medicationId] = qty.coerceAtLeast(1)
        _selectedQuantities.value = currentQuants
    }

    fun setNoteForMedication(medicationId: String, note: String) {
        val currentNotes = _selectedNotes.value.toMutableMap()
        currentNotes[medicationId] = note
        _selectedNotes.value = currentNotes
    }

    fun savePatientSettings(context: Any?, newSettings: PatientSettings) {
        viewModelScope.launch {
            var currentProfile = _activeProfile.value

            if (currentProfile == null) {
                val newProfile = Profile(
                    pazienteNome = newSettings.pazienteNome,
                    pazienteCf = newSettings.pazienteCf,
                    medicoNome = newSettings.medicoNome,
                    medicoTelefono = newSettings.medicoTelefono,
                    medicoEmail = newSettings.medicoEmail,
                    secondoIndirizzo = newSettings.secondoIndirizzo,
                    messaggioTesta = newSettings.messaggioTesta,
                    messaggioCoda = newSettings.messaggioCoda,
                    tipoInvio = newSettings.tipoInvio,
                    notificheAttive = newSettings.notificheAttive,
                    descrizioneNotifica = newSettings.descrizioneNotifica
                )
                db.insertProfile(newProfile)
                currentProfile = newProfile
            } else {
                val updatedProfile = currentProfile.copy(
                    pazienteNome = newSettings.pazienteNome,
                    pazienteCf = newSettings.pazienteCf,
                    medicoNome = newSettings.medicoNome,
                    medicoTelefono = newSettings.medicoTelefono,
                    medicoEmail = newSettings.medicoEmail,
                    secondoIndirizzo = newSettings.secondoIndirizzo,
                    messaggioTesta = newSettings.messaggioTesta,
                    messaggioCoda = newSettings.messaggioCoda,
                    tipoInvio = newSettings.tipoInvio,
                    notificheAttive = newSettings.notificheAttive,
                    descrizioneNotifica = newSettings.descrizioneNotifica
                )
                db.updateProfile(updatedProfile)
                currentProfile = updatedProfile
            }

            _activeProfileId.value = currentProfile.id
            _activeProfile.value = currentProfile
            db.setMetadataValue("active_profile_id", currentProfile.id)

            updateConfigStatus()

            // Update notifications
            val meds = db.getMedicationsSnapshotByProfile(currentProfile.id)
            com.aistudio.richfarmaciw.util.PlatformNotificationHelper.updateAllNotifications(
                context,
                meds,
                currentProfile.notificheAttive,
                currentProfile.descrizioneNotifica
            )

            val cfClean = newSettings.pazienteCf.replace(" ", "").uppercase()
            if (cfClean == "CLLRNN40M59L957V") {
                val medsInDb = db.getMedicationsSnapshotByProfile(currentProfile.id)
                if (medsInDb.isEmpty()) {
                    db.insertAllMedications(getDefaultMedicationsList(currentProfile.id))
                }
            }

            triggerAutoSync()
        }
    }

    private fun getDefaultMedicationsList(profileId: String) = listOf(
        Medication(profileId = profileId, nome = "Zanedip 10 mg", scatole = 2, note = "compresse"),
        Medication(profileId = profileId, nome = "Lasix 25 mg", scatole = 4, note = "compresse"),
        Medication(profileId = profileId, nome = "Pantoprazolo 20 mg", scatole = 1, note = "protettore stomaco"),
        Medication(profileId = profileId, nome = "Cardioaspirina 100 mg", scatole = 1, note = "dopo pranzo"),
        Medication(profileId = profileId, nome = "Zyloric 100 mg", scatole = 1, note = "compresse"),
        Medication(profileId = profileId, nome = "Tareg 160 mg", scatole = 1, note = "compresse della pressione"),
        Medication(profileId = profileId, nome = "Dibase 50.000 u.i./2,5ml", scatole = 1, note = "gocce orali")
    )

    fun startNewProfile() {
        _activeProfileId.value = null
        _activeProfile.value = null
        updateConfigStatus()
    }

    fun addProfile(name: String) {
        viewModelScope.launch {
            _activeProfileId.value = null
            _activeProfile.value = null
            _settings.value = PatientSettings(pazienteNome = name)
            _isConfigured.value = false
        }
    }

    fun deleteCurrentProfile() {
        viewModelScope.launch {
            _activeProfile.value?.let { profile ->
                db.deleteProfile(profile)
                _activeProfileId.value = null
                db.deleteMetadataKey("active_profile_id")

                val remainingProfiles = db.getAllProfilesSnapshot()
                if (remainingProfiles.isNotEmpty()) {
                    selectProfile(remainingProfiles.first().id)
                } else {
                    _activeProfile.value = null
                    updateConfigStatus()
                }
            }
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            db.deleteProfile(profile)
            if (_activeProfileId.value == profile.id) {
                _activeProfileId.value = null
                db.deleteMetadataKey("active_profile_id")
                val remainingProfiles = db.getAllProfilesSnapshot()
                if (remainingProfiles.isNotEmpty()) {
                    selectProfile(remainingProfiles.first().id)
                } else {
                    _activeProfile.value = null
                    updateConfigStatus()
                    _showSettings.value = true
                }
            }
            triggerAutoSync()
        }
    }

    fun addMedication(context: Any?, nome: String, scatole: Int, note: String, notificaAttiva: Boolean = false, orarioNotifica: String = "08:00", frequenzaValore: Int = 0, frequenzaTipo: String = "ORE") {
        viewModelScope.launch {
            val profileId = _activeProfileId.value ?: return@launch
            if (nome.isNotBlank()) {
                val newMed = Medication(
                    profileId = profileId,
                    nome = nome.trim(),
                    scatole = scatole.coerceAtLeast(0),
                    note = note.trim(),
                    inPausa = false,
                    notificaAttiva = notificaAttiva,
                    orarioNotifica = orarioNotifica,
                    frequenzaValore = frequenzaValore,
                    frequenzaTipo = frequenzaTipo
                )
                db.insertMedication(newMed)

                val profile = _activeProfile.value
                if (profile != null && profile.notificheAttive && newMed.notificaAttiva) {
                    com.aistudio.richfarmaciw.util.PlatformNotificationHelper.scheduleNotification(
                        context,
                        newMed,
                        profile.descrizioneNotifica
                    )
                }
                triggerAutoSync()
            }
        }
    }

    fun toggleMedicationStandby(context: Any?, medication: Medication) {
        viewModelScope.launch {
            val updated = medication.copy(inPausa = !medication.inPausa)
            db.updateMedication(updated)

            val profile = _activeProfile.value
            if (updated.inPausa) {
                com.aistudio.richfarmaciw.util.PlatformNotificationHelper.cancelNotification(context, updated)
                if (_selectedMedicationIds.value.contains(updated.id)) {
                    toggleMedicationSelection(updated)
                }
            } else if (profile != null && profile.notificheAttive && updated.notificaAttiva) {
                com.aistudio.richfarmaciw.util.PlatformNotificationHelper.scheduleNotification(
                    context,
                    updated,
                    profile.descrizioneNotifica
                )
            }
            triggerAutoSync()
        }
    }

    fun updateMedication(context: Any?, medication: Medication) {
        viewModelScope.launch {
            db.updateMedication(medication)
            val profile = _activeProfile.value
            if (profile != null && profile.notificheAttive && medication.notificaAttiva && !medication.inPausa) {
                com.aistudio.richfarmaciw.util.PlatformNotificationHelper.scheduleNotification(
                    context,
                    medication,
                    profile.descrizioneNotifica
                )
            } else {
                com.aistudio.richfarmaciw.util.PlatformNotificationHelper.cancelNotification(context, medication)
            }
            triggerAutoSync()
        }
    }

    fun deleteMedication(context: Any?, medication: Medication) {
        viewModelScope.launch {
            db.deleteMedication(medication)
            com.aistudio.richfarmaciw.util.PlatformNotificationHelper.cancelNotification(context, medication)
            if (_selectedMedicationIds.value.contains(medication.id)) {
                toggleMedicationSelection(medication)
            }
            triggerAutoSync()
        }
    }

    fun buildFormattedMessage(): String {
        val currentProfile = _settings.value
        val sections = mutableListOf<String>()

        if (currentProfile.messaggioTesta.isNotBlank()) {
            sections.add(currentProfile.messaggioTesta.trim())
        }

        val listMedications = medications.value
        val selectedIds = _selectedMedicationIds.value
        val medsList = listMedications.filter { selectedIds.contains(it.id) }.map { med ->
            val qty = _selectedQuantities.value[med.id] ?: med.scatole
            val tempNote = _selectedNotes.value[med.id] ?: ""
            val baseNote = med.note

            val noteStr = when {
                tempNote.isNotBlank() && baseNote.isNotBlank() -> " ($baseNote - $tempNote)"
                tempNote.isNotBlank() -> " ($tempNote)"
                baseNote.isNotBlank() -> " ($baseNote)"
                else -> ""
            }

            val scatolaWord = if (qty == 1) "scatola" else "scatole"
            "- ${med.nome}: $qty $scatolaWord$noteStr"
        }

        if (medsList.isNotEmpty()) {
            sections.add(medsList.joinToString("\n\n"))
        }

        val footerParts = mutableListOf<String>()
        footerParts.add("Nome Paziente: ${currentProfile.pazienteNome}")
        footerParts.add("Codice Fiscale: ${currentProfile.pazienteCf.uppercase()}")

        sections.add(footerParts.joinToString("\n"))

        if (currentProfile.messaggioCoda.isNotBlank()) {
            sections.add(currentProfile.messaggioCoda.trim())
        }

        return sections.joinToString("\n\n")
    }

    fun recordSentRequest(messageText: String) {
        viewModelScope.launch {
            val profileId = _activeProfileId.value ?: return@launch
            val currentProfile = _settings.value
            val listMedications = medications.value
            val selectedIds = _selectedMedicationIds.value

            val sentMeds = listMedications.filter { selectedIds.contains(it.id) }.map { med ->
                val qty = _selectedQuantities.value[med.id] ?: med.scatole
                val tempNote = _selectedNotes.value[med.id] ?: ""
                val baseNote = med.note
                val noteCombined = when {
                    tempNote.isNotBlank() && baseNote.isNotBlank() -> "$baseNote - $tempNote"
                    tempNote.isNotBlank() -> tempNote
                    else -> baseNote
                }
                SentMedication(
                    nome = med.nome,
                    scatole = qty,
                    note = noteCombined
                )
            }

            val req = SentRequest(
                profileId = profileId,
                data = "Oggi", // Semplificato per multiplatform commonMain
                pazienteNome = currentProfile.pazienteNome,
                medicoNome = currentProfile.medicoNome,
                farmaciSerialized = SentRequest.serializeMedicines(sentMeds),
                testoCompleto = messageText
            )

            db.insertSentRequest(req)
            clearFormSelection()
            triggerAutoSync()
        }
    }

    fun clearFormSelection() {
        _selectedMedicationIds.value = emptySet()
        _selectedQuantities.value = emptyMap()
        _selectedNotes.value = emptyMap()
    }

    fun deleteHistoryItem(request: SentRequest) {
        viewModelScope.launch {
            db.deleteSentRequest(request)
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    fun dismissAutoRestorePrompt() {
        _showAutoRestorePrompt.value = false
        if (_activeProfile.value == null) {
            _showSettings.value = true
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.createBackupJson()
            onResult(json)
        }
    }

    fun importBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.restoreFromJson(jsonString)
            if (result.isSuccess) {
                // Ricarica lo stato
                val activeId = db.getMetadataValue("active_profile_id")
                if (activeId != null) {
                    selectProfile(activeId)
                }
                _onboardingShown.value = db.getMetadataValue("onboarding_shown") == "true"
                _disclaimerAccepted.value = db.getMetadataValue("disclaimer_accepted") == "true"
                _donationCount.value = db.getMetadataValue("donation_count")?.toIntOrNull() ?: 0
                
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }
}
