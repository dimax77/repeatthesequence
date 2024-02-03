//MainActivity.kt
package com.example.rts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.rts.ui.theme.RTSTheme
//import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private val soundPlayer = SoundPlayer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RTSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(
                        viewModel = ViewModelProvider(
                            this,
                            GameViewModelFactory(soundPlayer)
                        ).get(GameViewModel::class.java)
                    )
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition", "MutableCollectionMutableState")
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val interactionSource = List(4) { MutableInteractionSource() }
    val coroutine = rememberCoroutineScope()
    var buttons = remember { mutableStateOf(mutableListOf<Button>()) }
    var isEnabled = remember{mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Current level: ${viewModel.state.value!!.currentLevel}")
        Text(text = "Top level: ${viewModel.state.value!!.topLevel}")
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        RowButton(
            viewModel = viewModel,
            indices = listOf(0, 1),
            interactionSource = interactionSource,
            isEnabled = isEnabled
        )
        RowButton(
            viewModel = viewModel,
            indices = listOf(2, 3),
            interactionSource = interactionSource,
            isEnabled = isEnabled
        )

    }
    Game(isEnabled = isEnabled, viewModel = viewModel, interactionSource = interactionSource)
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Game(isEnabled: MutableState<Boolean>, viewModel: GameViewModel, interactionSource: List<MutableInteractionSource>) {
    val randomSequence = viewModel.generateRandomSequence()
    var gameOver = false
    while (!gameOver) {
        PlaySequence(
            viewModel = viewModel,
            interactionSource = interactionSource,
            randomSequence = randomSequence
        )
        isEnabled.value = true
//        WaitForUserInput(interactionSource = interactionSource, viewModel = viewModel, randomSequence = randomSequence)
        if (viewModel.checkUserInput(randomSequence.toMutableList())) {
            viewModel.state.value!!.currentLevel++
        } else {
            gameOver = true
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun WaitForUserInput(
    interactionSource: List<MutableInteractionSource>,
    viewModel: GameViewModel,
    randomSequence: List<Int>
) {
    val coroutine = rememberCoroutineScope()
    coroutine.launch {
        while (true) {
            if (!(viewModel.state.value!!.userInput.size < randomSequence.size))
                break
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PlaySequence(
    viewModel: GameViewModel,
    interactionSource: List<MutableInteractionSource>,
    randomSequence: List<Int>
) {
    val coroutine = rememberCoroutineScope()
    coroutine.launch {
        for (buttonId in randomSequence) {

            delay(1000)
            val press = PressInteraction.Press(Offset.Zero)
            interactionSource[buttonId].emit(press)
            viewModel.onButtonClicked(buttonId)
            interactionSource[buttonId].emit(PressInteraction.Release(press))
        }
        viewModel.state.value!!.userPlaying = true
        delay(10000)
    }

}

@Composable
fun RowButton(
    viewModel: GameViewModel,
    indices: List<Int>,
    interactionSource: List<MutableInteractionSource>,
    isEnabled: MutableState<Boolean>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        for (idx in indices) {
//            val interactionSource: MutableInteractionSource =
//                remember { MutableInteractionSource() }

            Button(
                onClick = { viewModel.onButtonClicked(idx) },
                interactionSource = interactionSource[idx],
                modifier = Modifier.width(150.dp),
                enabled = isEnabled.value
            ) {
                Text(text = viewModel.state.value!!.buttons[idx].title)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    val context = LocalContext.current
    RTSTheme {
        GameScreen(
            viewModel = ViewModelProvider(
                LocalViewModelStoreOwner.current!!,
                GameViewModelFactory(SoundPlayer(context))
            ).get(GameViewModel::class.java)
        )
    }
}