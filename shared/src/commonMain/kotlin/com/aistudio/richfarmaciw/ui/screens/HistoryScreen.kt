package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.richfarmaciw.data.model.SentRequest
import com.aistudio.richfarmaciw.ui.theme.*
import com.aistudio.richfarmaciw.ui.viewmodel.MainViewModel
import org.jetbrains.compose.resources.painterResource
import richfarmaciw.shared.generated.resources.*

@Composable
fun HistoryScreen(viewModel: MainViewModel, manualText: String) {
    val requests by viewModel.sentRequests.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val profiles by viewModel.profiles.collectAsState()

    var showHelp by remember { mutableStateOf(false) }
    var selectedRequestForDialog by remember { mutableStateOf<SentRequest?>(null) }

    if (showHelp) {
        HelpDialog(type = "cronologia", manualText = manualText, onDismiss = { showHelp = false })
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
                                text = "Cronologia",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        IconButton(
                            onClick = { showHelp = true },
                            modifier = Modifier.size(36.dp).background(HelpIconBg, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Help,
                                contentDescription = "Aiuto",
                                tint = HelpIconTint,
                                modifier = Modifier.size(20.dp)
                            )
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

        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nessuna richiesta inviata.", color = Slate600, fontWeight = FontWeight.Medium)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(requests, key = { it.id }) { req ->
                        HistoryItemRow(
                            request = req,
                            onView = { selectedRequestForDialog = req },
                            onDelete = { viewModel.deleteHistoryItem(req) }
                        )
                    }
                }
            }
        }
    }

    if (selectedRequestForDialog != null) {
        val req = selectedRequestForDialog!!
        AlertDialog(
            onDismissRequest = { selectedRequestForDialog = null },
            title = { Text("Dettaglio Richiesta", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Paziente: ${req.pazienteNome}", fontWeight = FontWeight.SemiBold, color = Slate900)
                    Text("Medico: ${req.medicoNome}", color = Slate600)
                    HorizontalDivider(color = GrayBorder, modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = req.testoCompleto, color = Slate900, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRequestForDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("CHIUDI", color = White)
                }
            },
            containerColor = White
        )
    }
}

@Composable
fun HistoryItemRow(
    request: SentRequest,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Richiesta a: ${request.medicoNome}",
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 16.sp
                )
                Text(
                    text = "Paziente: ${request.pazienteNome}",
                    color = Slate600,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onView,
                    modifier = Modifier.size(34.dp).background(GrayBackground, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Visualizza",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).background(GrayBackground, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
