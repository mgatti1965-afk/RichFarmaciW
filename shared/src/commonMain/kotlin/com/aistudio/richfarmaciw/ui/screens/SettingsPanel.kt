package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.richfarmaciw.data.model.Medication
import com.aistudio.richfarmaciw.data.model.PatientSettings
import com.aistudio.richfarmaciw.data.model.Profile
import com.aistudio.richfarmaciw.ui.theme.*
import com.aistudio.richfarmaciw.ui.viewmodel.MainViewModel
import org.jetbrains.compose.resources.painterResource
import richfarmaciw.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelContent(
    settings: PatientSettings,
    medications: List<Medication>,
    viewModel: MainViewModel,
    manualText: String,
    onClose: () -> Unit
) {
    val profiles by viewModel.profiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val onboardingShown by viewModel.onboardingShown.collectAsState()

    var showOnboarding by remember(onboardingShown) { mutableStateOf(!onboardingShown) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var showHelp by remember { mutableStateOf(false) }
    var showWarningBanner by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showWarningBanner) {
        if (showWarningBanner) {
            kotlinx.coroutines.delay(3500)
            showWarningBanner = false
        }
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            kotlinx.coroutines.delay(2000)
            snackbarMessage = null
        }
    }

    if (showHelp) {
        HelpDialog(type = "configurazione", manualText = manualText, onDismiss = { showHelp = false })
    }

    if (showOnboarding) {
        OnboardingDialog(
            manualText = manualText,
            onDismiss = {
                showOnboarding = false
                viewModel.setOnboardingShown(true)
            }
        )
    }

    var pNome by remember(settings) { mutableStateOf(settings.pazienteNome) }
    var pCf by remember(settings) { mutableStateOf(settings.pazienteCf) }
    var mNome by remember(settings) { mutableStateOf(settings.medicoNome) }
    var mTel by remember(settings) { mutableStateOf(settings.medicoTelefono) }
    var mEmail by remember(settings) { mutableStateOf(settings.medicoEmail) }
    var secInd by remember(settings) { mutableStateOf(settings.secondoIndirizzo) }
    var msgTesta by remember(settings) { mutableStateOf(settings.messaggioTesta) }
    var msgCoda by remember(settings) { mutableStateOf(settings.messaggioCoda) }
    var valType by remember(settings) { mutableIntStateOf(settings.tipoInvio) }
    var nAttive by remember(settings) { mutableStateOf(settings.notificheAttive) }
    var nDesc by remember(settings) { mutableStateOf(settings.descrizioneNotifica) }

    var exitAttempted by remember(settings) { mutableStateOf(false) }
    val profileId by viewModel.activeProfileId.collectAsState()
    val isNewProfile = profileId == null

    val hasChanges by remember(pNome, pCf, mNome, mTel, mEmail, secInd, msgTesta, msgCoda, valType, nAttive, nDesc, settings) {
        derivedStateOf {
            pNome != settings.pazienteNome ||
            pCf != settings.pazienteCf ||
            mNome != settings.medicoNome ||
            mTel != settings.medicoTelefono ||
            mEmail != settings.medicoEmail ||
            secInd != settings.secondoIndirizzo ||
            msgTesta != settings.messaggioTesta ||
            msgCoda != settings.messaggioCoda ||
            valType != settings.tipoInvio ||
            nAttive != settings.notificheAttive ||
            nDesc != settings.descrizioneNotifica
        }
    }

    var showMedicationDialog by remember { mutableStateOf(false) }
    var medicationToEdit by remember { mutableStateOf<Medication?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }

    if (showBackupDialog) {
        BackupDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false },
            onImportSuccess = {
                snackbarMessage = "Backup importato con successo!"
                showBackupDialog = false
            }
        )
    }

    if (showMedicationDialog) {
        val platformContext = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current // generic anchor or context if needed, but App.kt passes actual context to provideRichFarmaciDatabase. For platform actions we can pass null or a generic platform handle if not on Android.
        MedicationEditorDialog(
            profileId = profileId ?: "",
            medication = medicationToEdit,
            notificationsEnabled = nAttive,
            onDismiss = { showMedicationDialog = false },
            onSave = { updatedMed ->
                if (medicationToEdit == null) {
                    viewModel.addMedication(
                        null,
                        updatedMed.nome,
                        updatedMed.scatole,
                        updatedMed.note,
                        updatedMed.notificaAttiva,
                        updatedMed.orarioNotifica,
                        updatedMed.frequenzaValore,
                        updatedMed.frequenzaTipo
                    )
                    snackbarMessage = "Farmaco aggiunto!"
                } else {
                    viewModel.updateMedication(null, updatedMed)
                    snackbarMessage = "Farmaco aggiornato!"
                }
                showMedicationDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(GrayBackground)) {
        Column(modifier = Modifier.fillMaxWidth().background(White).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        painter = painterResource(Res.drawable.app_icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "⚙️ Configurazione",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isNewProfile) {
                            Text(text = "CREAZIONE NUOVO PROFILO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { showBackupDialog = true }, modifier = Modifier.background(GrayBackground, CircleShape)) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Backup", tint = GreenPrimary)
                    }
                    IconButton(onClick = { showHelp = true }, modifier = Modifier.background(GrayBackground, CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Aiuto", tint = Slate900)
                    }
                    IconButton(
                        onClick = {
                            if (hasChanges && !exitAttempted) {
                                showWarningBanner = true
                                exitAttempted = true
                            } else {
                                onClose()
                            }
                        },
                        modifier = Modifier.background(GrayBackground, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = Slate900)
                    }
                }
            }
        }
        HorizontalDivider(color = GrayBorder)

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            item {
                ProfileContextSwitcher(
                    profiles = profiles,
                    activeProfile = activeProfile,
                    onProfileSelected = { viewModel.selectProfile(it) },
                    onAddProfile = { showAddProfileDialog = true },
                    onDeleteProfile = { viewModel.deleteProfile(it) }
                )
                HorizontalDivider(color = GrayBorder, modifier = Modifier.padding(top = 8.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "1. Anagrafica Paziente e Medico", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Slate900)

                    OutlinedTextField(
                        value = pNome,
                        onValueChange = { pNome = it },
                        label = { Text("Nome e Cognome Paziente (*)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GrayBorder,
                            focusedLabelColor = GreenPrimary,
                            unfocusedLabelColor = Slate600,
                            unfocusedContainerColor = White,
                            focusedContainerColor = White,
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = pCf,
                        onValueChange = { pCf = it.uppercase() },
                        label = { Text("Codice Fiscale (*)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GrayBorder,
                            focusedLabelColor = GreenPrimary,
                            unfocusedLabelColor = Slate600,
                            unfocusedContainerColor = White,
                            focusedContainerColor = White,
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(value = mNome, onValueChange = { mNome = it }, label = { Text("Nome Medico (*)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = GrayBorder, focusedLabelColor = GreenPrimary, unfocusedLabelColor = Slate600, unfocusedContainerColor = White, focusedContainerColor = White, focusedTextColor = Slate900, unfocusedTextColor = Slate900), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = mTel, onValueChange = { mTel = it }, label = { Text("Cellulare Medico (o Email*)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = GrayBorder, focusedLabelColor = GreenPrimary, unfocusedLabelColor = Slate600, unfocusedContainerColor = White, focusedContainerColor = White, focusedTextColor = Slate900, unfocusedTextColor = Slate900), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = mEmail, onValueChange = { mEmail = it }, label = { Text("Email Medico (o Cellulare*)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = GrayBorder, focusedLabelColor = GreenPrimary, unfocusedLabelColor = Slate600, unfocusedContainerColor = White, focusedContainerColor = White, focusedTextColor = Slate900, unfocusedTextColor = Slate900), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = secInd, onValueChange = { secInd = it }, label = { Text("Ulteriori note (Opzionale)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = GrayBorder, focusedLabelColor = GreenPrimary, unfocusedLabelColor = Slate600, unfocusedContainerColor = White, focusedContainerColor = White, focusedTextColor = Slate900, unfocusedTextColor = Slate900), shape = RoundedCornerShape(12.dp))

                    HorizontalDivider(color = GrayBorder)
                    Text(text = "2. Opzioni Messaggio", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(top = 10.dp))

                    OutlinedTextField(value = msgTesta, onValueChange = { msgTesta = it }, label = { Text("Frase di Testa") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = GrayBorder, focusedLabelColor = GreenPrimary, unfocusedLabelColor = Slate600, unfocusedContainerColor = White, focusedContainerColor = White, focusedTextColor = Slate900, unfocusedTextColor = Slate900), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = msgCoda, onValueChange = { msgCoda = it }, label = { Text("Frase di Coda") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = GrayBorder, focusedLabelColor = GreenPrimary, unfocusedLabelColor = Slate600, unfocusedContainerColor = White, focusedContainerColor = White, focusedTextColor = Slate900, unfocusedTextColor = Slate900), shape = RoundedCornerShape(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tipo Invio Predefinito:", fontWeight = FontWeight.Bold, color = Slate900)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("WhatsApp", "SMS", "Email").forEachIndexed { index, label ->
                                val selected = valType == index
                                Surface(
                                    modifier = Modifier.weight(1f).height(48.dp).clickable { valType = index },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) White else GreenPrimary,
                                    border = BorderStroke(2.dp, if (selected) GreenPrimary else GreenPrimary.copy(alpha = 0.1f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            label,
                                            color = if (selected) GreenPrimary else White,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Attiva Notifiche", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Switch(
                                checked = nAttive,
                                onCheckedChange = { nAttive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = White,
                                    checkedTrackColor = GreenPrimary,
                                    uncheckedThumbColor = White,
                                    uncheckedTrackColor = GrayBorder
                                )
                            )
                        }
                        if (nAttive) {
                            OutlinedTextField(
                                value = nDesc,
                                onValueChange = { nDesc = it },
                                label = { Text("Descrizione Notifica") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    unfocusedBorderColor = GrayBorder,
                                    focusedLabelColor = GreenPrimary,
                                    unfocusedLabelColor = Slate600,
                                    unfocusedContainerColor = White,
                                    focusedContainerColor = White,
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val missingFields = mutableListOf<String>()
                            if (pNome.isBlank()) missingFields.add("Nome Paziente")
                            if (pCf.isBlank()) missingFields.add("Codice Fiscale")
                            if (mNome.isBlank()) missingFields.add("Nome Medico")

                            if (valType == 0 || valType == 1) {
                                if (mTel.isBlank()) missingFields.add("Cellulare Medico")
                            } else if (valType == 2) {
                                if (mEmail.isBlank()) missingFields.add("Email Medico")
                            }

                            if (missingFields.isEmpty()) {
                                viewModel.savePatientSettings(null, PatientSettings(pazienteNome = pNome, pazienteCf = pCf, medicoNome = mNome, medicoTelefono = mTel, medicoEmail = mEmail, secondoIndirizzo = secInd, messaggioTesta = msgTesta, messaggioCoda = msgCoda, tipoInvio = valType, notificheAttive = nAttive, descrizioneNotifica = nDesc))
                                snackbarMessage = "Profilo salvato correttamente!"
                                exitAttempted = false
                            } else {
                                snackbarMessage = "Mancano dati obbligatori:\n" + missingFields.joinToString(", ")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasChanges) Color(0xFFD32F2F) else Color.Gray,
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = hasChanges
                    ) {
                        Text(
                            "SALVA CONFIGURAZIONE",
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }

            if (!isNewProfile) {
                item {
                    HorizontalDivider(color = GrayBorder)
                    Text(text = "3. Gestore Rubrica Farmaci", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(top = 10.dp))
                }
                item {
                    Button(
                        onClick = {
                            medicationToEdit = null
                            showMedicationDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NUOVO FARMACO/NOTIFICA", fontWeight = FontWeight.Bold, color = White)
                    }
                }
                item { Text("I Tuoi Farmaci Salvati:", fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(top = 8.dp)) }
                if (medications.isEmpty()) {
                    item { Text("La rubrica è vuota.", color = Slate600, modifier = Modifier.padding(16.dp)) }
                } else {
                    items(medications, key = { it.id }) { med ->
                        ConfigurationMedicationRow(
                            med = med,
                            notificationsEnabled = settings.notificheAttive,
                            onEdit = {
                                medicationToEdit = med
                                showMedicationDialog = true
                            },
                            onToggleStandby = { viewModel.toggleMedicationStandby(null, med) },
                            onDelete = { viewModel.deleteMedication(null, med) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showAddProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = { Text("Nuovo Profilo") },
            text = {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text("Nome Paziente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayBorder,
                        focusedLabelColor = GreenPrimary,
                        unfocusedLabelColor = Slate600,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newProfileName.isNotBlank()) {
                        viewModel.addProfile(newProfileName)
                        newProfileName = ""
                        showAddProfileDialog = false
                    }
                }) {
                    Text("AGGIUNGI", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) {
                    Text("ANNULLA")
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showWarningBanner,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 90.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Color.Red,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "Modifiche non salvate. Premi ancora per uscire.",
                    color = White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    snackbarMessage?.let { msg ->
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = msg,
                    color = White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ConfigurationMedicationRow(
    med: Medication,
    notificationsEnabled: Boolean,
    onEdit: () -> Unit,
    onToggleStandby: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (med.inPausa) GrayBackground else White),
        border = BorderStroke(1.dp, GrayBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = med.nome,
                    fontWeight = FontWeight.Bold,
                    color = if (med.inPausa) Slate600 else Slate900,
                    fontSize = 16.sp
                )
                if (med.note.isNotBlank()) {
                    Text(text = med.note, fontSize = 13.sp, color = Slate600)
                }
                Text(text = "Scatole standard: ${med.scatole}", fontSize = 12.sp, color = Slate600)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleStandby) {
                    Icon(
                        imageVector = if (med.inPausa) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Sospendi/Riprendi",
                        tint = if (med.inPausa) GreenPrimary else Slate600
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Slate600)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun MedicationEditorDialog(
    profileId: String,
    medication: Medication?,
    notificationsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    var nome by remember { mutableStateOf(medication?.nome ?: "") }
    var scatole by remember { mutableIntStateOf(medication?.scatole ?: 1) }
    var note by remember { mutableStateOf(medication?.note ?: "") }
    var notificaActiva by remember { mutableStateOf(medication?.notificaAttiva ?: false) }
    var orarioNotifica by remember { mutableStateOf(medication?.orarioNotifica ?: "08:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (medication == null) "Nuovo Farmaco" else "Modifica Farmaco", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Farmaco (*)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Dosaggio (Opzionale)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scatole standard da richiedere:", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (scatole > 1) scatole-- }) { Icon(Icons.Default.Remove, contentDescription = null) }
                        Text(text = scatole.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { scatole++ }) { Icon(Icons.Default.Add, contentDescription = null) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        onSave(
                            Medication(
                                id = medication?.id ?: com.aistudio.richfarmaciw.data.model.generateId(),
                                profileId = profileId,
                                nome = nome.trim(),
                                scatole = scatole,
                                note = note.trim(),
                                inPausa = medication?.inPausa ?: false,
                                notificaAttiva = notificaActiva,
                                orarioNotifica = orarioNotifica,
                                frequenzaValore = medication?.frequenzaValore ?: 1,
                                frequenzaTipo = medication?.frequenzaTipo ?: "GIORNI"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = nome.isNotBlank()
            ) {
                Text("SALVA", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ANNULLA", color = Slate600) }
        },
        containerColor = White
    )
}
