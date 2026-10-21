package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.richfarmaciw.data.model.Medication
import com.aistudio.richfarmaciw.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import richfarmaciw.shared.generated.resources.*
import com.aistudio.richfarmaciw.ui.viewmodel.MainViewModel
import org.jetbrains.compose.resources.painterResource
import richfarmaciw.shared.generated.resources.*

@Composable
fun RequestScreen(
    viewModel: MainViewModel,
    manualText: String,
    onSendMessage: (String, String) -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val selectedIds by viewModel.selectedMedicationIds.collectAsState()
    val selectedQuantities by viewModel.selectedQuantities.collectAsState()
    val donationCount by viewModel.donationCount.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()

    var showHelp by remember { mutableStateOf(false) }
    var showDonation by remember { mutableStateOf(false) }
    var showQuickMsgDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var quickMsgText by remember { mutableStateOf("") }

    if (showHelp) {
        HelpDialog(type = "richiesta", manualText = manualText, onDismiss = { showHelp = false })
    }

    if (showPreviewDialog) {
        val previewMessage = viewModel.buildFormattedMessage()
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = { Text("Anteprima Messaggio", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Verifica il testo prima di inviarlo al medico:",
                        fontSize = 14.sp,
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        color = GrayBackground.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = previewMessage,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = viewModel.getRequestUriString()
                        viewModel.recordSentRequest(previewMessage)
                        onSendMessage(uri, previewMessage)
                        showPreviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("CONFERMA E INVIA", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("MODIFICA", color = Slate600)
                }
            }
        )
    }

    if (showDonation) {
        DonationDialog(
            donationCount = donationCount,
            appName = "RichFarmaci",
            onDismiss = { showDonation = false },
            onConfirm = {
                viewModel.incrementDonationCount()
                val url = "https://www.paypal.com/donate/?hosted_button_id=YOUR_ID"
                onSendMessage(url, url)
                showDonation = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(GrayBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PharmacyCross(modifier = Modifier.size(28.dp), color = GreenPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "RichFarmaci",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showDonation = true },
                                modifier = Modifier.size(36.dp).background(CoffeeIconBg, CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_coffee),
                                    contentDescription = "Dona",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { showHelp = true },
                                modifier = Modifier.size(36.dp).background(HelpIconBg, CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_help),
                                    contentDescription = "Aiuto",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setShowSettings(true) },
                                modifier = Modifier.size(36.dp).background(GrayBackground, CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_settings),
                                    contentDescription = "Impostazioni",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    ProfileContextSwitcher(
                        profiles = profiles,
                        activeProfile = activeProfile,
                        onProfileSelected = { viewModel.selectProfile(it) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        text = "Seleziona i farmaci da richiedere:",
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                val activeMeds = medications.filter { !it.inPausa }
                if (activeMeds.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GrayBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GreenPrimary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Nessun farmaco attivo.", fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Aggiungili nelle impostazioni per iniziare.", textAlign = TextAlign.Center, color = Slate600)
                            }
                        }
                    }
                } else {
                    items(activeMeds, key = { it.id }) { med ->
                        val isSelected = selectedIds.contains(med.id)
                        val qty = selectedQuantities[med.id] ?: med.scatole.coerceAtLeast(1)

                        MedicationRequestRow(
                            med = med,
                            isSelected = isSelected,
                            quantity = qty,
                            onToggle = { viewModel.toggleMedicationSelection(med) },
                            onQtyChange = { viewModel.setQuantityForMedication(med.id, it) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Action Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 10.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(16.dp)) {
                    if (selectedIds.isNotEmpty()) {
                        Button(
                            onClick = { showPreviewDialog = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("INVIA RICHIESTA AL MEDICO", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showQuickMsgDialog = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            border = BorderStroke(1.5.dp, GreenPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Messaggio veloce al Medico", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    if (showQuickMsgDialog) {
        AlertDialog(
            onDismissRequest = { showQuickMsgDialog = false },
            title = { Text("Messaggio Veloce", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = quickMsgText,
                    onValueChange = { quickMsgText = it },
                    placeholder = { Text("Es: Ho la febbre, cosa posso prendere?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickMsgText.isNotBlank()) {
                            val uri = viewModel.getQuickMessageUriString(quickMsgText)
                            val plainText = viewModel.buildQuickMessage(quickMsgText)
                            onSendMessage(uri, plainText)
                            showQuickMsgDialog = false
                            quickMsgText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("INVIA", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickMsgDialog = false }) {
                    Text("ANNULLA", color = Slate600)
                }
            }
        )
    }
}

@Composable
fun MedicationRequestRow(
    med: Medication,
    isSelected: Boolean,
    quantity: Int,
    onToggle: () -> Unit,
    onQtyChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) GreenLightBg else White),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) GreenPrimary else GrayBorder),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenPrimary,
                    uncheckedColor = Slate600.copy(alpha = 0.5f)
                )
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = med.nome,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 16.sp
                )
                if (med.note.isNotBlank()) {
                    Text(
                        text = med.note,
                        fontSize = 13.sp,
                        color = Slate600
                    )
                }
            }
            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) onQtyChange(quantity - 1) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(GreenPrimary.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Rimuovi",
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.ExtraBold,
                            color = White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = { onQtyChange(quantity + 1) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(GreenPrimary.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aggiungi",
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GrayBackground,
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Text(
                        text = "$quantity pz",
                        color = Slate600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
