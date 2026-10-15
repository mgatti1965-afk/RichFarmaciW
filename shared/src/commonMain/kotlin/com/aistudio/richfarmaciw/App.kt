package com.aistudio.richfarmaciw

import androidx.compose.runtime.*
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
        MainScreen(viewModel = viewModel)
    }
}
