package com.example.rts.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.rts.data.GameModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RowButton(
    viewModel: GameViewModel,
    data: GameModel,
    indices: List<Int>,
    interactionSource: List<MutableInteractionSource>,
    isEnabled: MutableState<Boolean>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
    ) {
        for (idx in indices) {
            Button(
                onClick = { viewModel.onButtonClicked(idx) },
                interactionSource = interactionSource[idx],
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .padding(16.dp),
                enabled = isEnabled.value,
                colors = ButtonDefaults.buttonColors(
                    containerColor = data.buttons[idx].color,
                    disabledContainerColor = data.buttons[idx].color,
                    disabledContentColor = Color.Black
                ),

                shape = RoundedCornerShape(28.dp)
            ) {
                Text(text = data.buttons[idx].title)
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel, navigateToMainMenu: () -> Unit) {
    val gameData by viewModel.gameState.collectAsState()

    val gameOver = gameData.gameOver
    val waitForUserInput = gameData.waitForUserInput

    val interactionSource = remember { List(4) { MutableInteractionSource() } }
    val isEnabledForClick = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Current level: ${gameData.currentLevel.intValue}")
            Text(text = "Top level: ${gameData.topLevel}")
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            RowButton(
                viewModel = viewModel,
                data = gameData,
                indices = listOf(0, 1),
                interactionSource = interactionSource,
                isEnabled = isEnabledForClick,
            )
            RowButton(
                viewModel = viewModel,
                data = gameData,
                indices = listOf(2, 3),
                interactionSource = interactionSource,
                isEnabled = isEnabledForClick,

                )

        }
    }

    if (!gameOver.value && !waitForUserInput.value && isEnabledForClick.value) {
        isEnabledForClick.value = false
        playSequence(
            viewModel = viewModel,
            interactionSource = interactionSource,
            randomSequence = viewModel.randomSequence.value!!,
            isEnabledForClick
        )
    } else if (gameOver.value) {
        ShowGameOverDialog(
            viewModel,
            gameData.currentLevel.intValue,
            onRestart = { /*TODO*/ },
            onHome = navigateToMainMenu
        )
    }
}

private fun playSequence(
    viewModel: GameViewModel,
    interactionSource: List<MutableInteractionSource>,
    randomSequence: List<Int>,
    isEnabled: MutableState<Boolean>
) {
    viewModel.viewModelScope.launch {
        for (buttonId in randomSequence) {
            delay(1000)
            val press = PressInteraction.Press(Offset.Zero)
            interactionSource[buttonId].emit(press)
            viewModel.onButtonClicked(buttonId)
            interactionSource[buttonId].emit(PressInteraction.Release(press))
        }
        delay(1000)
        isEnabled.value = true
        viewModel.setWaitForUserInput()
    }
}

@Composable
fun ShowGameOverDialog(
    viewModel: GameViewModel,
    currentLevel: Int,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    var dialogVisible by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = {
//            dialogVisible = false
//            onRestart()
        },
        title = { Text("Game Over") },
        text = { Text("Your level: $currentLevel") },
        confirmButton = {
            Button(onClick = {
                dialogVisible = false
                onHome()
            }) {
                Text("Main Menu")
            }
            Button(onClick = {
                viewModel.resetState()
                dialogVisible = false
                onRestart()
            }) {
                Text("Restart")
            }
        })
}