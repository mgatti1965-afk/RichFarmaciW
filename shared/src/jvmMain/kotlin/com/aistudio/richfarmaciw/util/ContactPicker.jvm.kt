package com.aistudio.richfarmaciw.util

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var showDialog by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("") }
    var inputPhone by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Importa Dati Medico", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Inserisci o incolla i dati del medico:",
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nome e Cognome Medico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { inputPhone = it },
                        label = { Text("Cellulare / Email Medico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextButton(
                        onClick = {
                            try {
                                val chooser = JFileChooser().apply {
                                    dialogTitle = "Seleziona File Contatto (.vcf, .txt)"
                                    isAcceptAllFileFilterUsed = true
                                    fileFilter = FileNameExtensionFilter("File Contatti (*.vcf, *.vcard, *.txt)", "vcf", "vcard", "txt")
                                }
                                val result = chooser.showOpenDialog(null)
                                if (result == JFileChooser.APPROVE_OPTION) {
                                    val file = chooser.selectedFile
                                    if (file != null && file.exists()) {
                                        val info = parseVCardFile(file)
                                        if (info != null) {
                                            inputName = info.name
                                            inputPhone = info.phoneOrEmail
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("📁 Oppure carica da file .vcf", color = Color(0xFF059669))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank() || inputPhone.isNotBlank()) {
                            currentOnContactPicked(ContactInfo(inputName.trim(), inputPhone.trim()))
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("INSERISCI", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("ANNULLA")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    return remember {
        DesktopContactPickerLauncher {
            inputName = ""
            inputPhone = ""
            showDialog = true
        }
    }
}

private fun parseVCardFile(file: File): ContactInfo? {
    var name = ""
    var phone = ""
    var email = ""

    try {
        val lines = file.readLines()
        for (line in lines) {
            val trimmed = line.trim()
            val upper = trimmed.uppercase()

            if (upper.startsWith("FN:") || upper.startsWith("FN;")) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    name = trimmed.substring(colonIdx + 1).trim()
                }
            } else if (name.isBlank() && (upper.startsWith("N:") || upper.startsWith("N;"))) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    val parts = trimmed.substring(colonIdx + 1).split(";")
                    val cleanParts = parts.map { it.trim() }.filter { it.isNotBlank() }
                    name = cleanParts.reversed().joinToString(" ")
                }
            } else if (upper.startsWith("TEL") || upper.contains("TEL;")) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    val rawNum = trimmed.substring(colonIdx + 1).trim()
                    if (phone.isBlank()) {
                        phone = rawNum
                    }
                }
            } else if (upper.startsWith("EMAIL") || upper.contains("EMAIL;")) {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx >= 0) {
                    val rawEmail = trimmed.substring(colonIdx + 1).trim()
                    if (email.isBlank()) {
                        email = rawEmail
                    }
                }
            }
        }

        if (name.isBlank() && phone.isBlank() && lines.isNotEmpty()) {
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isBlank()) continue

                if (phone.isBlank() && trimmed.matches(".*[0-9]{5,}.*".toRegex())) {
                    phone = trimmed
                } else if (name.isBlank()) {
                    name = trimmed
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val cleanPhone = if (phone.isNotBlank()) phone.replace("[^0-9+]".toRegex(), "") else email.trim()

    return if (name.isNotBlank() || cleanPhone.isNotBlank()) {
        ContactInfo(
            name = name.trim(),
            phoneOrEmail = cleanPhone
        )
    } else null
}
