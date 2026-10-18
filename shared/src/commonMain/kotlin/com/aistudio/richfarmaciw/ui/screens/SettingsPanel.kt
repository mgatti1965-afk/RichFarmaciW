package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
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

    Column(
        modifier = Modifier.fillMaxSize().background(GrayBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Slate900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Configurazione",
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
                                Icon(Icons.Default.CloudUpload, contentDescription = "Backup", tint = Slate900)
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
            }
        }
        HorizontalDivider(color = GrayBorder)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
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

                    val focusManager = LocalFocusManager.current
                    Button(
                        onClick = {
                            focusManager.clearFocus()
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
                        Text("NUOVO FARMACO", fontWeight = FontWeight.Bold, color = White)
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
        border = BorderStroke(1.dp, if (med.inPausa) GrayBorder else GreenPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = med.nome,
                        fontWeight = FontWeight.Bold,
                        color = if (med.inPausa) Slate600 else Slate900,
                        fontSize = 16.sp
                    )
                }
                if (med.note.isNotBlank()) {
                    Text(text = med.note, fontSize = 13.sp, color = Slate600)
                }
                Text(
                    text = "Scatole predefinite: ${med.scatole}",
                    fontSize = 12.sp,
                    color = Slate600,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (notificationsEnabled && med.notificaAttiva) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GreenPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Notifica ore ${med.orarioNotifica}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.clickable { onToggleStandby() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (med.inPausa) GreenPrimary else Color(0xFFD1D5DB)
                ) {
                    Text(
                        text = if (med.inPausa) "Riattiva" else "Pausa",
                        color = if (med.inPausa) White else Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp).background(GrayBackground, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).background(Color(0xFFFFEBEE), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
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
    var frequenzaValore by remember { mutableIntStateOf(medication?.frequenzaValore ?: 1) }
    var frequenzaTipo by remember { mutableStateOf(medication?.frequenzaTipo ?: "GIORNI") }
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guida Farmaco e Notifiche", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Nome Farmaco: Inserisci il nome commerciale del farmaco.", fontSize = 13.sp)
                    Text("• N. scatole: Indica quante scatole richiedere standard.", fontSize = 13.sp)
                    Text("• Note: Aggiungi i dosaggi (es. 1 compressa dopo i pasti).", fontSize = 13.sp)
                    if (notificationsEnabled) {
                        Text("• Attiva Notifica: Imposta orario e ripetizione (ogni X giorni o ore) per ricevere i promemoria.", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                    Text("HO CAPITO", color = White)
                }
            },
            containerColor = White
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (medication == null) "Nuovo Farmaco" else "Modifica Farmaco",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.size(32.dp).background(GreenPrimary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Aiuto",
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).background(GrayBackground, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = Slate900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 580.dp).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Farmaco (*)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("N. scatole:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (scatole > 1) scatole-- },
                            modifier = Modifier.size(36.dp).background(Slate900, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Meno", tint = White)
                        }
                        Text(text = scatole.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 12.dp))
                        IconButton(
                            onClick = { scatole++ },
                            modifier = Modifier.size(36.dp).background(GreenPrimary, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Più", tint = White)
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (es: dopo i pasti)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (notificationsEnabled) {
                    HorizontalDivider(color = GrayBorder)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { notificaActiva = !notificaActiva }.padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = notificaActiva,
                            onCheckedChange = { notificaActiva = it },
                            colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attiva Notifica", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    }

                    if (notificaActiva) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Orari:", fontSize = 13.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GrayBorder),
                                color = White
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(orarioNotifica, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = { /* add time */ },
                                modifier = Modifier.size(36.dp).background(GreenPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aggiungi orario", tint = White)
                            }
                        }

                        Text("Ripetizione:", fontSize = 13.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GrayBorder)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (frequenzaValore > 1) frequenzaValore-- }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("$frequenzaValore", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { frequenzaValore++ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Text(if (frequenzaTipo == "ORE") "ore" else "gg", color = Slate900, fontWeight = FontWeight.Medium)

                            Row(modifier = Modifier.padding(start = 8.dp)) {
                                Surface(
                                    modifier = Modifier.clickable { frequenzaTipo = "ORE" },
                                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                                    color = if (frequenzaTipo == "ORE") GreenPrimary else White,
                                    border = BorderStroke(1.dp, if (frequenzaTipo == "ORE") GreenPrimary else GrayBorder)
                                ) {
                                    Text("Ore", color = if (frequenzaTipo == "ORE") White else Slate900, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                                Surface(
                                    modifier = Modifier.clickable { frequenzaTipo = "GIORNI" },
                                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                    color = if (frequenzaTipo == "GIORNI") GreenPrimary else White,
                                    border = BorderStroke(1.dp, if (frequenzaTipo == "GIORNI") GreenPrimary else GrayBorder)
                                ) {
                                    Text("GG", color = if (frequenzaTipo == "GIORNI") White else Slate900, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }

                        // Prossime Notifiche preview cards dynamically computed
                        val step = frequenzaValore.coerceAtLeast(1)
                        val dynamicPreviews = if (frequenzaTipo == "ORE") {
                            val (d1, t1) = calculateNotificationPreviewTime(orarioNotifica, 0)
                            val (d2, t2) = calculateNotificationPreviewTime(orarioNotifica, step)
                            val (d3, t3) = calculateNotificationPreviewTime(orarioNotifica, step * 2)
                            listOf(
                                Triple("PROSSIMA", d1, t1),
                                Triple("2ª NOTIFICA", d2, t2),
                                Triple("3ª NOTIFICA", d3, t3)
                            )
                        } else {
                            val d1 = "Oggi"
                            val d2 = if (step == 1) "Domani" else "+$step gg"
                            val d3 = if (step == 1) "+2 gg" else "+${step * 2} gg"
                            listOf(
                                Triple("PROSSIMA", d1, orarioNotifica),
                                Triple("2ª NOTIFICA", d2, orarioNotifica),
                                Triple("3ª NOTIFICA", d3, orarioNotifica)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrayBackground.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Prossime Notifiche:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    dynamicPreviews.forEach { item ->
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = White),
                                            border = BorderStroke(1.dp, GrayBorder),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp).fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(item.first, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenPrimary, maxLines = 1)
                                                Text(item.second, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate900)
                                                Text(item.third, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                notificaAttiva = if (notificationsEnabled) notificaActiva else false,
                                orarioNotifica = orarioNotifica,
                                frequenzaValore = frequenzaValore,
                                frequenzaTipo = frequenzaTipo
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

private fun calculateNotificationPreviewTime(baseTime: String, addHours: Int): Pair<String, String> {
    val parts = baseTime.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val totalHours = h + addHours
    val days = totalHours / 24
    val finalHour = totalHours % 24

    val hh = if (finalHour < 10) "0$finalHour" else "$finalHour"
    val mm = if (m < 10) "0$m" else "$m"
    val formattedTime = "$hh:$mm"

    val dayLabel = when {
        addHours == 0 -> "Oggi"
        days == 0 -> "+$addHours ore"
        days == 1 -> "Domani"
        else -> "+$days gg"
    }

    return Pair(dayLabel, formattedTime)
}
