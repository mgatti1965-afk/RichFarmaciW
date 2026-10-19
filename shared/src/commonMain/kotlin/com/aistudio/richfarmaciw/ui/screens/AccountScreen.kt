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
import com.aistudio.richfarmaciw.ui.theme.Slate900

@Composable
fun AccountSection(
    accountState: AccountState,
    isSyncing: Boolean,
    onCreateAccount: () -> Unit,
    onJoinAccount: (String) -> Unit,
    onDisconnectAccount: () -> Unit,
    onSyncNow: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showJoinDialog by remember { mutableStateOf(false) }
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
                        text = "Accedi agli stessi farmaci da Cellulare e PC",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            if (accountState.isConnected && accountState.familyCode.isNotBlank()) {
                // Stato: Connesso con un Codice Famiglia
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
                        Text(
                            text = "IL TUO CODICE ACCOUNT FAMIGLIA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = accountState.familyCode,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = "Usa questo codice sugli altri tuoi dispositivi per sincronizzare i dati automaticamente.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(accountState.familyCode))
                                    copiedMessage = true
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Copia Codice", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onSyncNow,
                                enabled = !isSyncing,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Sincronizza", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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

                TextButton(
                    onClick = onDisconnectAccount,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(6.dp))
                    Text("Scollega questo dispositivo dall'Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }

            } else {
                // Stato: Non ancora collegato
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Per accedere ai tuoi farmaci e richieste sia da smartphone che da PC, collega i dispositivi con un Codice Account.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = onCreateAccount,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Crea Nuovo Codice Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showJoinDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ho già un Codice (Collega)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            icon = { Icon(Icons.Default.Key, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Inserisci il Codice Account", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Digita il codice presente sull'altro tuo dispositivo:")
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        label = { Text("Codice Account Famiglia") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            onJoinAccount(inputCode)
                            showJoinDialog = false
                            inputCode = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Collega Dispositivo", fontWeight = FontWeight.Bold)
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
