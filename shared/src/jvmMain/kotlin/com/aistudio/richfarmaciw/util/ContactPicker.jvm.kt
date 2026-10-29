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
import java.io.File
import java.awt.Desktop
import java.net.URI

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
    var contactList by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    var showNoContactsMessage by remember { mutableStateOf(false) }

    fun loadFromSystemOrGoogleRubrica() {
        val contacts = fetchGoogleAndWindowsContacts()
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

    if (showNoContactsMessage) {
        AlertDialog(
            onDismissRequest = { showNoContactsMessage = false },
            title = {
                Text("Nessuna Anagrafica in Rubrica", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Nessuna anagrafica o contatto trovato nella rubrica di sistema o Google Contatti.",
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoContactsMessage = false
                        try {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(URI("https://contacts.google.com"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("APRI GOOGLE CONTATTI", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoContactsMessage = false }) {
                    Text("OK", color = Color(0xFF475569))
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
            loadFromSystemOrGoogleRubrica()
        }
    }
}

private fun fetchGoogleAndWindowsContacts(): List<ContactInfo> {
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
