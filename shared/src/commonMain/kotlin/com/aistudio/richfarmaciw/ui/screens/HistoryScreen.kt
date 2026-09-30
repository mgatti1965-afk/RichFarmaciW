package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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

    Column(modifier = Modifier.fillMaxSize().background(GrayBackground)) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.app_icon),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cronologia",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Aiuto", tint = Slate900)
                    }
                }

                ProfileContextSwitcher(
                    profiles = profiles,
                    activeProfile = activeProfile,
                    onProfileSelected = { viewModel.selectProfile(it) }
                )
            }
        }

        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nessuna richiesta inviata.", color = Slate600, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
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
                    fontSize = 14.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, contentDescription = "Visualizza", tint = Slate600)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}
