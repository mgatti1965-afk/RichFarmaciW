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
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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

    fun pickFromFile() {
        try {
            val chooser = JFileChooser().apply {
                dialogTitle = "Seleziona File Rubrica / Contatti (.vcf, .vcard, .txt)"
                isAcceptAllFileFilterUsed = true
                fileFilter = FileNameExtensionFilter("File Contatti (*.vcf, *.vcard, *.txt)", "vcf", "vcard", "txt")
            }
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = chooser.selectedFile
                if (file != null && file.exists()) {
                    val parsed = parseAllVCardContacts(file)
                    if (parsed.isNotEmpty()) {
                        if (parsed.size == 1) {
                            currentOnContactPicked(parsed.first())
                        } else {
                            contactList = parsed
                            showSelectionDialog = true
                        }
                    } else {
                        showNoContactsMessage = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showNoContactsMessage = true
        }
    }

    if (showNoContactsMessage) {
        AlertDialog(
            onDismissRequest = { showNoContactsMessage = false },
            title = {
                Text("Nessun Contatto Trovato", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Non sono state trovate anagrafiche valide nella rubrica o nel file selezionato.",
                    color = Color(0xFF475569)
                )
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

    if (showSelectionDialog && contactList.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = {
                Text("Seleziona Contatto Medico", fontWeight = FontWeight.Bold)
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
            pickFromFile()
        }
    }
}

private fun parseAllVCardContacts(file: File): List<ContactInfo> {
    val results = mutableListOf<ContactInfo>()
    try {
        val lines = file.readLines()
        var currentName = ""
        var currentPhone = ""
        var currentEmail = ""

        fun flushCurrent() {
            val cleanPhone = if (currentPhone.isNotBlank()) {
                currentPhone.replace("[^0-9+]".toRegex(), "")
            } else {
                currentEmail.trim()
            }
            if (currentName.isNotBlank() || cleanPhone.isNotBlank()) {
                results.add(ContactInfo(currentName.trim(), cleanPhone))
            }
            currentName = ""
            currentPhone = ""
            currentEmail = ""
        }

        for (line in lines) {
            val trimmed = line.trim()
            val upper = trimmed.uppercase()

            if (upper.startsWith("BEGIN:VCARD")) {
                currentName = ""
                currentPhone = ""
                currentEmail = ""
            } else if (upper.startsWith("END:VCARD")) {
                flushCurrent()
            } else if (upper.startsWith("FN:") || upper.startsWith("FN;")) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    currentName = trimmed.substring(colonIdx + 1).trim()
                }
            } else if (currentName.isBlank() && (upper.startsWith("N:") || upper.startsWith("N;"))) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    val parts = trimmed.substring(colonIdx + 1).split(";")
                    val cleanParts = parts.map { it.trim() }.filter { it.isNotBlank() }
                    currentName = cleanParts.reversed().joinToString(" ")
                }
            } else if (upper.startsWith("TEL") || upper.contains("TEL;")) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    val rawNum = trimmed.substring(colonIdx + 1).trim()
                    if (currentPhone.isBlank()) {
                        currentPhone = rawNum
                    }
                }
            } else if (upper.startsWith("EMAIL") || upper.contains("EMAIL;")) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    val rawEmail = trimmed.substring(colonIdx + 1).trim()
                    if (currentEmail.isBlank()) {
                        currentEmail = rawEmail
                    }
                }
            }
        }

        if (results.isEmpty() && (currentName.isNotBlank() || currentPhone.isNotBlank() || currentEmail.isNotBlank())) {
            flushCurrent()
        }

        if (results.isEmpty()) {
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isBlank()) continue
                if (trimmed.matches(".*[0-9]{5,}.*".toRegex())) {
                    results.add(ContactInfo("Contatto Rubrica", trimmed.replace("[^0-9+]".toRegex(), "")))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return results
}
