package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.richfarmaciw.data.model.AccountState
import com.aistudio.richfarmaciw.ui.theme.GreenAccent
import com.aistudio.richfarmaciw.ui.theme.GreenPrimary

@Composable
fun AccountSection(
    accountState: AccountState,
    isSyncing: Boolean,
    onJoinAccount: (String) -> Unit,
    onSyncNow: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }
    var copiedMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = GreenPrimary
                )
                Column {
                    Text(
                        text = "Sincronizzazione Cloud Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "I tuoi dati sono sincronizzati tra Smartphone e PC",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "SINCRONIZZAZIONE AUTOMATICA ATTIVA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Account: ${accountState.familyCode.ifBlank { "RichFarmaci" }}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Tutti i farmaci inseriti su questo dispositivo compaiono automaticamente anche sugli altri tuoi dispositivi.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onSyncNow,
                            enabled = !isSyncing,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Sincronizzazione in corso...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Aggiorna / Sincronizza Ora", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (copiedMessage) {
                        Text(
                            text = "✓ Codice copiato negli appunti!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                    }
                }
            }

            if (accountState.syncErrorMessage.isNotBlank()) {
                Text(
                    text = accountState.syncErrorMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            // Opzioni avanzate (per i familiari)
            TextButton(
                onClick = { showAdvancedOptions = !showAdvancedOptions },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (showAdvancedOptions) "Nascondi Opzioni Avanzate" else "Opzioni Avanzate Codice Account (Per Familiari)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showAdvancedOptions) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(accountState.familyCode.ifBlank { "RichFarmaci" }))
                            copiedMessage = true
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copia Codice Account", fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = { showJoinDialog = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Collega ad un altro Codice Personalizzato", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            icon = { Icon(Icons.Default.Key, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Personalizza Codice Account", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Digita il codice personalizzato che desideri utilizzare:")
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        label = { Text("Codice Account (Default: RichFarmaci)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onJoinAccount(inputCode.ifBlank { "RichFarmaci" })
                        showJoinDialog = false
                        inputCode = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Salva Codice", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}
