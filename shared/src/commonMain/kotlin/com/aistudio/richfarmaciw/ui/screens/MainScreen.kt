package com.aistudio.richfarmaciw.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import com.aistudio.richfarmaciw.ui.theme.GreenPrimary
import com.aistudio.richfarmaciw.ui.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource
import richfarmaciw.shared.generated.resources.*
import org.jetbrains.compose.resources.readResourceBytes

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.aistudio.richfarmaciw.getPlatform
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val isConfigured by viewModel.isConfigured.collectAsState()
    val disclaimerAccepted by viewModel.disclaimerAccepted.collectAsState()

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val platform = remember { getPlatform() }

    var manualText by remember { mutableStateOf("") }
    @OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
    LaunchedEffect(Unit) {
        try {
            manualText = Res.readBytes("files/manuale.md").decodeToString()
        } catch (e: Exception) {
            manualText = "# Manuale\nErrore nel caricamento del manuale."
        }
    }

    if (!disclaimerAccepted) {
        DisclaimerDialog(
            manualText = manualText,
            onAccept = { viewModel.setDisclaimerAccepted(true) },
            onExitApp = { /* Multiplatform exit strategy */ }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isConfigured && !showSettings) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        NavigationBar(
                            modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth(),
                            containerColor = Color.White,
                            contentColor = GreenPrimary
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { viewModel.selectTab(0) },
                                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(Res.string.request_title)) },
                                label = { Text(stringResource(Res.string.request_title)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GreenPrimary,
                                    selectedTextColor = GreenPrimary,
                                    indicatorColor = GreenPrimary.copy(alpha = 0.1f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { viewModel.selectTab(1) },
                                icon = { Icon(Icons.Default.History, contentDescription = stringResource(Res.string.history_title)) },
                                label = { Text(stringResource(Res.string.history_title)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GreenPrimary,
                                    selectedTextColor = GreenPrimary,
                                    indicatorColor = GreenPrimary.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (showSettings) {
                val settings by viewModel.settings.collectAsState()
                val medications by viewModel.medications.collectAsState()

                SettingsPanelContent(
                    settings = settings,
                    medications = medications,
                    viewModel = viewModel,
                    manualText = manualText,
                    onClose = { viewModel.setShowSettings(false) }
                )
            } else {
                when (currentTab) {
                    0 -> RequestScreen(
                        viewModel = viewModel,
                        manualText = manualText,
                        onSendMessage = { uri, plainText ->
                            try {
                                // Copiamo sempre negli appunti come backup di sicurezza
                                clipboardManager.setText(AnnotatedString(plainText))

                                val isWeb = !platform.isMobile

                                if (isWeb) {
                                    // Su Web, spesso gli schemi whatsapp:// o sms: falliscono silenziosamente
                                    // Se è WhatsApp, usiamo l'URL universale wa.me se possibile,
                                    // altrimenti segnaliamo il successo della copia.
                                    if (uri.contains("whatsapp")) {
                                        uriHandler.openUri(uri)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Messaggio copiato. Apertura WhatsApp Web...")
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Testo della richiesta copiato negli appunti.")
                                        }
                                        // Proviamo comunque ad aprire l'URI di sistema (es. mailto o sms)
                                        try { uriHandler.openUri(uri) } catch (_: Exception) {}
                                    }
                                } else {
                                    // Su Mobile (Android/iOS)
                                    uriHandler.openUri(uri)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Richiesta inviata.")
                                    }
                                }
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Copiato negli appunti (App di sistema non trovata).")
                                }
                            }
                        }
                    )
                    1 -> HistoryScreen(
                        viewModel = viewModel,
                        manualText = manualText
                    )
                }
            }
        }
    }
}

