package com.example.rts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    navigateToGame: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToAbout: () -> Unit,
    resetViewmodelState: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = {
            resetViewmodelState()
            navigateToGame()
        }) {
            Text(text = "New Game")
        }
        Button(onClick = { navigateToSettings() }) {
            Text(text = "Settings")
        }
        Button(onClick = { navigateToAbout() }) {
            Text(text = "About")
        }
    }
}