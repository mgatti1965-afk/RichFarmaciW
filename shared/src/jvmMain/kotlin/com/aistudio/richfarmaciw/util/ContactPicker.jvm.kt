package com.aistudio.richfarmaciw.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File

class DesktopContactPickerLauncher(
    private val onLaunch: () -> Unit
) : ContactPickerLauncher {
    override val isAvailable: Boolean = true
    override fun launch() {
        onLaunch()
    }
}

@Composable
actual fun rememberContactPicker(
    onContactPicked: (ContactInfo) -> Unit
): ContactPickerLauncher {
    val currentOnContactPicked by rememberUpdatedState(onContactPicked)
    val scope = rememberCoroutineScope()

    var contactList by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }
    var showAuthPromptDialog by remember { mutableStateOf(false) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    var showNoContactsMessage by remember { mutableStateOf(false) }
    var showClientIdConfigDialog by remember { mutableStateOf(false) }
    var inputClientId by remember { mutableStateOf(GoogleContactsService.userClientId) }
    var isLoading by remember { mutableStateOf(false) }

    fun startGoogleAuth() {
        showAuthPromptDialog = false
        scope.launch {
            isLoading = true
            var contacts = GoogleContactsService.fetchContacts()
            if (contacts.isEmpty()) {
                contacts = getWindowsSystemContacts()
            }
            isLoading = false

            if (contacts.isNotEmpty()) {
                if (contacts.size == 1) {
                    currentOnContactPicked(contacts.first())
                } else {
                    contactList = contacts
                    showSelectionDialog = true
                }
            } else {
                showNoContactsMessage = true
            }
        }
    }

    if (showAuthPromptDialog) {
        AlertDialog(
            onDismissRequest = { showAuthPromptDialog = false },
            title = {
                Text("Accesso a Google Contatti", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "RichFarmaciW desidera accedere alla tua rubrica per selezionare i dati del Medico curante. Desideri aprire la pagina di autorizzazione Google?",
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { startGoogleAuth() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CONSENTI CON GOOGLE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthPromptDialog = false }) {
                    Text("ANNULLA", color = Color(0xFF475569))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Accesso a Google Contatti", fontWeight = FontWeight.Bold)
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Color(0xFF059669), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("In attesa di autorizzazione da Google...", color = Color(0xFF475569))
                }
            },
            confirmButton = {},
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    if (showNoContactsMessage) {
        AlertDialog(
            onDismissRequest = { showNoContactsMessage = false },
            title = {
                Text("Nessuna Anagrafica in Rubrica", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Nessuna anagrafica trovata nella rubrica o autorizzazione non completata.",
                        color = Color(0xFF475569)
                    )
                    TextButton(onClick = {
                        showNoContactsMessage = false
                        showClientIdConfigDialog = true
                    }) {
                        Text("⚙️ Configura ID Client Google OAuth", color = Color(0xFF059669), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNoContactsMessage = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    if (showClientIdConfigDialog) {
        AlertDialog(
            onDismissRequest = { showClientIdConfigDialog = false },
            title = {
                Text("Configurazione Client ID Google", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Per accedere a Google Contatti da app desktop, inserisci il tuo Client ID OAuth2 di Google Cloud:",
                        color = Color(0xFF475569),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = inputClientId,
                        onValueChange = { inputClientId = it },
                        label = { Text("Client ID Google Cloud") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        GoogleContactsService.userClientId = inputClientId.trim()
                        showClientIdConfigDialog = false
                        startGoogleAuth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SALVA E CONNETTI", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClientIdConfigDialog = false }) {
                    Text("ANNULLA", color = Color(0xFF475569))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    if (showSelectionDialog && contactList.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = {
                Text("Seleziona Medico dalla Rubrica", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contactList) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentOnContactPicked(contact)
                                    showSelectionDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = contact.name.ifBlank { "Senza Nome" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (contact.phoneOrEmail.isNotBlank()) {
                                        Text(
                                            text = contact.phoneOrEmail,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectionDialog = false }) {
                    Text("ANNULLA")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    return remember {
        DesktopContactPickerLauncher {
            showAuthPromptDialog = true
        }
    }
}

private fun getWindowsSystemContacts(): List<ContactInfo> {
    val results = mutableListOf<ContactInfo>()
    try {
        val userProfile = System.getenv("USERPROFILE") ?: System.getProperty("user.home") ?: return emptyList()
        val contactsDir = File(userProfile, "Contacts")

        if (contactsDir.exists() && contactsDir.isDirectory) {
            val contactFiles = contactsDir.listFiles() ?: emptyArray()

            for (file in contactFiles) {
                try {
                    val info = parseSingleWindowsContactFile(file)
                    if (info != null) {
                        results.add(info)
                    }
                } catch (_: Exception) {}
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return results
}

private fun parseSingleWindowsContactFile(file: File): ContactInfo? {
    var name = ""
    var phone = ""
    var email = ""

    try {
        val content = file.readText()

        if (file.name.lowercase().endsWith(".contact")) {
            val formattedNameMatch = Regex("<c[a-zA-Z0-9:]*FormattedName[^>]*>(.*?)</", RegexOption.IGNORE_CASE).find(content)
            if (formattedNameMatch != null) {
                name = formattedNameMatch.groupValues[1].trim()
            }

            val phoneMatch = Regex("<c[a-zA-Z0-9:]*Number[^>]*>(.*?)</", RegexOption.IGNORE_CASE).find(content)
            if (phoneMatch != null) {
                phone = phoneMatch.groupValues[1].trim()
            }

            val emailMatch = Regex("<c[a-zA-Z0-9:]*Address[^>]*>(.*?)</", RegexOption.IGNORE_CASE).find(content)
            if (emailMatch != null) {
                email = emailMatch.groupValues[1].trim()
            }
        } else {
            val lines = content.lines()
            for (line in lines) {
                val trimmed = line.trim()
                val upper = trimmed.uppercase()
                if (upper.startsWith("FN:") || upper.startsWith("FN;")) {
                    val colonIdx = trimmed.indexOf(":")
                    if (colonIdx >= 0) name = trimmed.substring(colonIdx + 1).trim()
                } else if (upper.startsWith("TEL") || upper.contains("TEL;")) {
                    val colonIdx = trimmed.indexOf(":")
                    if (colonIdx >= 0 && phone.isBlank()) phone = trimmed.substring(colonIdx + 1).trim()
                } else if (upper.startsWith("EMAIL") || upper.contains("EMAIL;")) {
                    val colonIdx = trimmed.indexOf(":")
                    if (colonIdx >= 0 && email.isBlank()) email = trimmed.substring(colonIdx + 1).trim()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val cleanPhone = if (phone.isNotBlank()) phone.replace("[^0-9+]".toRegex(), "") else email.trim()

    return if (name.isNotBlank() || cleanPhone.isNotBlank()) {
        ContactInfo(name = name.ifBlank { file.nameWithoutExtension }, phoneOrEmail = cleanPhone)
    } else null
}
