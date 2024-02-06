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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {
    private val soundPlayer = SoundPlayer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RTSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(
                        viewModel = ViewModelProvider(
                            this, GameViewModelFactory(this, soundPlayer)
                        )[GameViewModel::class.java]
                    )
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val interactionSource = List(4) { MutableInteractionSource() }
    val isEnabled = remember { mutableStateOf(false) }
    val coroutineScope = CoroutineScope(Dispatchers.Main)
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
    LaunchedEffect(viewModel.state.value!!.currentLevel.intValue) {
        runGame(
            isEnabled,
            viewModel,
            interactionSource,
            coroutineScope,
            viewModel.randomSequence.value!!
        )
    }
}

fun runGame(
    isEnabled: MutableState<Boolean>,
    viewModel: GameViewModel,
    interactionSource: List<MutableInteractionSource>,
    coroutineScope: CoroutineScope,
    randomSequence: List<Int>
) {
    coroutineScope.launch {
        val playSequenceJob = playSequence(
            viewModel = viewModel,
            interactionSource = interactionSource,
            randomSequence = randomSequence
        )
        playSequenceJob.join()
        isEnabled.value = true
        delay(3000)

        while (viewModel.state.value!!.userInput.size < randomSequence.size)
            delay(100)
        isEnabled.value = false
        val userInputMatch = viewModel.checkUserInput(viewModel.randomSequence.value!!)
        if (userInputMatch) {

            if (viewModel.state.value!!.currentLevel.intValue > viewModel.state.value!!.topLevel.intValue) viewModel.state.value!!.topLevel =
                viewModel.state.value!!.currentLevel
            viewModel.updateRandomSequence()
            viewModel.clearUserInput()
            Log.d("user input size:", "${viewModel.state.value?.userInput?.size}")
            viewModel.state.value!!.currentLevel.intValue++
        } else {
            Log.d("Random Sequence: ", "$randomSequence")
            Log.d("User Input: ", "${viewModel.state.value!!.userInput}")
            viewModel.resetState()
            exitProcess(0)
        }
    }
}
//
//fun waitForUserInput(
//    viewModel: GameViewModel,
//    randomSequence: List<Int>,
//) {
//    viewModel.state.observeForever {
//        if (viewModel.state.value!!.userInput.size >= randomSequence.size) return@observeForever
//    }
//}

fun playSequence(
    viewModel: GameViewModel,
    interactionSource: List<MutableInteractionSource>,
    randomSequence: List<Int>
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
                modifier = Modifier.width(150.dp).height(150.dp).padding(16.dp),
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
            viewModel = ViewModelProvider(
                LocalViewModelStoreOwner.current!!,
                GameViewModelFactory(context, SoundPlayer(context))
            ).get(GameViewModel::class.java)
        )
    }
}