package com.aistudio.richfarmaciw

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aistudio.richfarmaciw.data.db.provideRichFarmaciDatabase
import com.aistudio.richfarmaciw.ui.screens.MainScreen
import com.aistudio.richfarmaciw.ui.theme.RichFarmaciTheme
import com.aistudio.richfarmaciw.ui.viewmodel.MainViewModel

@Composable
fun App(context: Any? = null) {
    val database = remember {
        provideRichFarmaciDatabase(context)
    }
    val viewModel = remember(database) {
        MainViewModel(database)
    }

    RichFarmaciTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(viewModel = viewModel)
        }
    }
}
