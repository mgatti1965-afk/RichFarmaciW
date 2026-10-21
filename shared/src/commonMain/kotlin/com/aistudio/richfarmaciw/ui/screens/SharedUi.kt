package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.richfarmaciw.data.model.Profile
import com.aistudio.richfarmaciw.ui.theme.*
import com.aistudio.richfarmaciw.ui.viewmodel.MainViewModel
import com.aistudio.richfarmaciw.util.saveBackupFile
import com.aistudio.richfarmaciw.util.readBackupFile
import org.jetbrains.compose.resources.painterResource
import richfarmaciw.shared.generated.resources.*

@Composable
fun DisclaimerDialog(manualText: String, onAccept: () -> Unit, onExitApp: () -> Unit) {
    var showFullManual by remember { mutableStateOf(false) }

    if (showFullManual) {
        ManualDialog(manualText = manualText, onDismiss = { showFullManual = false })
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Limitazione di Responsabilità", fontWeight = FontWeight.Bold, color = Color.Red.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "L’applicazione RichFarmaci ha scopo puramente gestionale e di supporto logistico. Non costituisce un dispositivo medico e non sostituisce in alcun modo il parere, la diagnosi o il consiglio del medico curante.",
                    fontSize = 13.sp,
                    color = Slate600,
                    lineHeight = 18.sp
                )
                Text(
                    "Lo sviluppatore non si assume alcuna responsabilità per:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )
                HelpItem("Errori, ritardi o mancate consegne dei messaggi (WhatsApp, SMS, Email).", fontSize = 13.sp)
                HelpItem("Malfunzionamento delle notifiche (dovute a risparmio energetico o sistema operativo).", fontSize = 13.sp)
                HelpItem("Errori nei dati (nomi farmaci, dosaggi, CF) inseriti dall'utente.", fontSize = 13.sp)
                Text(
                    "L'utente è tenuto a verificare sempre l'effettivo invio delle richieste. L'uso dell'app avviene sotto la piena e consapevole responsabilità dell'utente e a proprio rischio.",
                    fontSize = 13.sp,
                    color = Slate600,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showFullManual = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manuale", color = White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ho capito", color = White, fontWeight = FontWeight.Bold)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = White
    )
}

@Composable
fun PharmacyCross(modifier: Modifier = Modifier, color: Color = GreenPrimary) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight(0.3f)
                .fillMaxWidth(),
            color = color,
            shape = RoundedCornerShape(2.dp)
        ) {}
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .fillMaxHeight(),
            color = color,
            shape = RoundedCornerShape(2.dp)
        ) {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HelpDialog(type: String, manualText: String, onDismiss: () -> Unit) {
    var showFullManual by remember { mutableStateOf(false) }

    if (showFullManual) {
        ManualDialog(manualText = manualText, onDismiss = { showFullManual = false })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(HelpIconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        tint = HelpIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = when(type) { "richiesta" -> "Guida Richiesta"; "cronologia" -> "Guida Cronologia"; else -> "Guida Configurazione" }, fontWeight = FontWeight.Bold, color = Slate900)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when(type) {
                    "richiesta" -> {
                        HelpItem("Scegli il paziente tramite la barra azzurra in alto (se ne gestisci più di uno).")
                        HelpItem("Seleziona i farmaci cliccando sul loro nome e regola il numero delle scatole con + e -.")
                        HelpItem("Premi 'Invia richiesta al Medico' per visualizzare l'anteprima del testo.")
                        HelpItem("Nella schermata di anteprima puoi confermare l'invio definitivo via WhatsApp, SMS o Email.")
                        HelpItem("In caso di necessità puoi utilizzare \"Messaggio veloce al Medico\" per comunicazioni extra.")
                        HelpItemContent {
                            Text("Se desideri sostenere il progetto, clicca sull'icona ", fontSize = 15.sp, color = Slate600)
                            Box(
                                modifier = Modifier.size(22.dp).background(CoffeeIconBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = "Sostieni",
                                    tint = CoffeeIconTint,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(" in alto a destra.", fontSize = 15.sp, color = Slate600)
                        }
                    }
                    "cronologia" -> {
                        HelpItem("Scegli il paziente tramite la barra azzurra in alto (se ne gestisci più di uno).")
                        HelpItem("Le richieste sono visualizzate in ordine cronologico, dalla più recente alla più vecchia.")
                        HelpItem("Usa 'Visualizza' per leggere il testo completo del messaggio inviato.")
                    }
                    "configurazione" -> {
                        HelpItem("Usa la barra azzurra superiore per gestire i profili (aggiungi o elimina).")
                        HelpItem("BACKUP: Usa l'icona della nuvola ☁️ in alto per esportare o importare i tuoi dati.")
                        HelpItem("1. ANAGRAFICA: Inserisci i dati del Paziente e del Medico.")
                        HelpItem("2. TIPO INVIO: Scegli la tipologia (WhatsApp/SMS/Email).")
                        HelpItem("3. NOTIFICHE: Attiva i promemoria per i farmaci.")
                        HelpItem("SALVATAGGIO: Il tasto diventa ROSSO se ci sono modifiche.")
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showFullManual = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manuale", color = White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ho capito", color = White, fontWeight = FontWeight.Bold)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GrayBackground
    )
}

@Composable
fun OnboardingDialog(manualText: String, onDismiss: () -> Unit) {
    var showFullManual by remember { mutableStateOf(false) }

    if (showFullManual) {
        ManualDialog(manualText = manualText, onDismiss = { showFullManual = false })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Benvenuto in RichFarmaci! 🏥",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Slate900,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Configura il tuo profilo in pochi passi:",
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
                HelpItem("Inserisci Nome, Codice Fiscale e dati del Medico.")
                HelpItem("Scegli il metodo di invio e salva la configurazione.")
                HelpItem("Aggiungi i tuoi Farmaci e attiva i promemoria.")

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = GrayBorder.copy(alpha = 0.5f))

                Text(
                    "LIMITAZIONE DI RESPONSABILITÀ:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Red.copy(alpha = 0.7f)
                )
                Text(
                    "L'app ha scopo logistico e non sostituisce il medico. Lo sviluppatore non risponde di mancati invii o errori. L'uso è a tuo rischio.",
                    fontSize = 12.sp,
                    color = Slate600,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showFullManual = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manuale", color = White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ho capito", color = White, fontWeight = FontWeight.Bold)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = White
    )
}

@Composable
fun BackupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onImportSuccess: () -> Unit
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var downloadMsg by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backup e Ripristino", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Usa questa funzione per scaricare un file con tutti i dati o per ripristinarli dalla cartella Download.",
                    fontSize = 14.sp,
                    color = Slate600
                )

                Button(
                    onClick = {
                        isProcessing = true
                        errorMsg = null
                        downloadMsg = null
                        viewModel.exportBackup { json ->
                            val fileName = "RichFarmaciW_Backup.json"
                            val saved = saveBackupFile(fileName, json)
                            isProcessing = false
                            if (saved) {
                                downloadMsg = "File 'RichFarmaciW_Backup.json' salvato in Download!"
                            } else {
                                errorMsg = "Errore durante il salvataggio del file."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SCARICA FILE (JSON)", color = White)
                }

                Button(
                    onClick = {
                        isProcessing = true
                        errorMsg = null
                        downloadMsg = null
                        readBackupFile { jsonContent ->
                            if (!jsonContent.isNullOrBlank()) {
                                viewModel.importBackup(jsonContent) { success ->
                                    isProcessing = false
                                    if (success) {
                                        onImportSuccess()
                                    } else {
                                        errorMsg = "Errore: File di backup non valido o corrotto."
                                    }
                                }
                            } else {
                                isProcessing = false
                                errorMsg = "Nessun file di backup trovato nella cartella Download."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("IMPORTA FILE", color = White)
                }

                if (downloadMsg != null) {
                    Text(
                        text = downloadMsg!!,
                        color = GreenPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CHIUDI", color = Slate600)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = White
    )
}

@Composable
fun ProfileContextSwitcher(
    profiles: List<Profile>,
    activeProfile: Profile?,
    onProfileSelected: (String) -> Unit,
    onAddProfile: (() -> Unit)? = null,
    onDeleteProfile: ((Profile) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = GreenLightBg.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activeProfile?.pazienteNome?.ifBlank { "Seleziona Profilo" } ?: "Seleziona Profilo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (activeProfile?.pazienteCf?.isNotBlank() == true) "C.F. ${activeProfile.pazienteCf}" else "Paziente attivo",
                            fontSize = 11.sp,
                            color = Slate600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .background(White)
        ) {
            profiles.forEach { profile ->
                DropdownMenuItem(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                profile.pazienteNome,
                                color = Slate900,
                                fontSize = 13.sp,
                                fontWeight = if(profile.id == activeProfile?.id) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (onDeleteProfile != null && profiles.size > 1) {
                                IconButton(
                                    onClick = {
                                        onDeleteProfile(profile)
                                        expanded = false
                                    },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    },
                    onClick = {
                        onProfileSelected(profile.id)
                        expanded = false
                    }
                )
            }
            if (onAddProfile != null) {
                HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))
                DropdownMenuItem(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aggiungi nuovo paziente", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        onAddProfile()
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun HelpItem(text: String, fontSize: androidx.compose.ui.unit.TextUnit = 15.sp) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Text("• ", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = (fontSize.value + 3).sp)
        Text(text = text, fontSize = fontSize, color = Slate600)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HelpItemContent(
    fontSize: TextUnit = 15.sp,
    content: @Composable FlowRowScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Text("• ", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = (fontSize.value + 3).sp)
        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)
        ) {
            content()
        }
    }
}

@Composable
fun DonationDialog(
    donationCount: Int,
    appName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isBlocked = donationCount >= 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isBlocked) "Grazie di cuore! ☕" else "Offri un caffè ☕",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Slate900
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (isBlocked)
                        "Hai già sostenuto il progetto. Ti ringraziamo immensamente per il tuo supporto!"
                    else "Sostieni lo sviluppo di $appName con un contributo di 5€ per un buon caffè.\n\nVerrai reindirizzato su una pagina sicura gestita da PayPal dove potrai scegliere:\n• Se hai un account PayPal, usalo per procedere velocemente.\n• Se NON hai un account, potrai procedere comodamente con la tua carta di credito o prepagata cliccando su 'Paga con una carta'.",
                    fontSize = 16.sp,
                    color = Slate600
                )
            }
        },
        confirmButton = {
            if (!isBlocked) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "SOSTIENI CON 5€",
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (isBlocked) "CHIUDI" else "ANNULLA", color = Slate600)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = White
    )
}
