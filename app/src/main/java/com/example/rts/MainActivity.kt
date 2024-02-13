//MainActivity.kt
package com.example.rts

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.rts.ui.theme.RTSTheme
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {
    private val soundPlayer = SoundPlayer(this)
    private val interactionSource = List(4) { MutableInteractionSource() }
    private lateinit var viewModel: GameViewModel
    private lateinit var coroutineScope: CoroutineScope

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this, GameViewModelFactory(this, soundPlayer)
        )[GameViewModel::class.java]
        coroutineScope = viewModel.viewModelScope

        setContent {
            RTSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) { GameScreen() }
            }
        }
    }

    @Composable
    fun GameScreen() {
        val waitForUserInput = remember { mutableStateOf(false) }
        val isEnabled = remember { mutableStateOf(false) }
        val gameOver = remember { mutableStateOf(viewModel.state.value!!.gameOver) }
        val currentLevel = remember { mutableIntStateOf(viewModel.state.value!!.currentLevel.intValue) }

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Current level: ${viewModel.state.value!!.currentLevel.intValue}")
            Text(text = "Top level: ${viewModel.state.value!!.topLevel.intValue}")
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
//        viewModel.state.observeAsState().value?.waitForUserInput?.value?.let {
//            if (it) {
//                runGame(isEnabled, viewModel, coroutineScope, viewModel.randomSequence.value!!)
//                viewModel.checkUserInput()
//                Log.d("Observer", "Wait for user input")
//            }
//
//        }
        SideEffect {
            currentLevel.value = viewModel.state.value!!.currentLevel.intValue
        }

        SideEffect {
            gameOver.value = viewModel.state.value!!.gameOver
        }
        LaunchedEffect(viewModel.state.value) {
            if (viewModel.state.value!!.gameOver) {
                gameOver.value = true
            }
        }
//        SideEffect() {
//            currentLevel.intValue = viewModel.state.value!!.currentLevel.intValue
//        }
        LaunchedEffect(currentLevel.value) {
            if(!gameOver.value) {
                Log.d("Current level updated", "$currentLevel")
                playSequence(
                    viewModel = viewModel,
                    interactionSource = interactionSource,
                    randomSequence = viewModel.randomSequence.value!!,
                    isEnabled
                )
            }

        }

//        LaunchedEffect(
//            rememberUpdatedState(waitForUserInput.value),
//            rememberUpdatedState(viewModel.state.value!!.currentLevel.intValue)
//        ) {
//            Log.d("GameLog", "LaunchedEffect triggered")
//            if (waitForUserInput.value) {
//                Log.d("rauGame Launched Effect", "trying run Game")
//                runGame(isEnabled, viewModel, coroutineScope, viewModel.randomSequence.value!!)
//            }
//        }

        if (gameOver.value) {
            showGameOverDialog(viewModel.state.value!!.currentLevel.intValue)
        }
        waitForUserInput.value = false
    }

    private fun playSequence(
        viewModel: GameViewModel,
        interactionSource: List<MutableInteractionSource>,
        randomSequence: List<Int>,
        isEnabled: MutableState<Boolean>
    ): Job {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        return coroutineScope.launch {
            for (buttonId in randomSequence) {
                delay(1000)
                val press = PressInteraction.Press(Offset.Zero)
                interactionSource[buttonId].emit(press)
                viewModel.onButtonClicked(buttonId)
                interactionSource[buttonId].emit(PressInteraction.Release(press))
            }
            viewModel.state.value!!.userPlaying = true

            delay(2000)
            isEnabled.value = true
            viewModel.setWaitForUserInput(true)
        }
    }

    @Composable
    fun showGameOverDialog(currentLevel: Int) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over") },
            text = { Text("Your level: $currentLevel") },
            confirmButton = {
                Button(onClick = { }) {
                    Text("Restart")
                }
            })
    }

    private fun runGame(
        isEnabled: MutableState<Boolean>,
        viewModel: GameViewModel,
        coroutineScope: CoroutineScope,
        randomSequence: List<Int>
    ) {
        coroutineScope.launch {
            isEnabled.value = true
            delay(3000)
            while (viewModel.state.value!!.userInput.size < randomSequence.size && !viewModel.state.value?.gameOver!!)
                delay(100)
            isEnabled.value = false
            if (viewModel.state.value!!.currentLevel.intValue > viewModel.state.value!!.topLevel.intValue)
                viewModel.state.value!!.topLevel.intValue =
                    viewModel.state.value!!.currentLevel.intValue + 1
            viewModel.state.value?.userInput?.clear()
            viewModel.updateRandomSequence()
            viewModel.state.value!!.currentLevel.intValue++
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
                    colors = ButtonDefaults.buttonColors(viewModel.state.value!!.buttons[idx].color)
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
//                viewModel = ViewModelProvider(
//                    LocalViewModelStoreOwner.current!!,
//                    GameViewModelFactory(context, SoundPlayer(context))
//                ).get(GameViewModel::class.java)
            )
        }
    }
}
