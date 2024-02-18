//MainActivity.kt
package com.example.rts

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.rts.ui.theme.RTSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

const val TOP_LEVEL_VALUE = "top_level"

class MainActivity : ComponentActivity() {
    private val soundPlayer = SoundPlayer(this)
    private val interactionSource = List(4) { MutableInteractionSource() }
    private lateinit var viewModel: GameViewModel
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(
            this, GameViewModelFactory(this, soundPlayer, getTopLevelValue())
        )[GameViewModel::class.java]
        coroutineScope = viewModel.viewModelScope


        setContent {
            RTSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "Home") {
                        composable("Home") {
                            HomeScreen(navController)
                        }
                        composable("Game") {
                            GameScreen(navController)
                        }
                        composable("About") {
                            AboutScreen(navController, viewModel)
                        }
                    }
//                    GameScreen()
                }
            }
        }
    }

    private fun getTopLevelValue(): Int {
        return sharedPreferences.getInt(TOP_LEVEL_VALUE, 1)
    }

    private fun updateSharedPreferences(newTopLevel: Int) {
        if (getTopLevelValue() < newTopLevel) {
            with(sharedPreferences.edit()) {
                putInt(TOP_LEVEL_VALUE, newTopLevel)
                apply()
            }
        }
    }


    @Composable
    fun HomeScreen(navController: NavController) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { navController.navigate("Game") }) {
                Text(text = "New Game")
            }
            Button(onClick = { navController.navigate("About") }) {
                Text(text = "About")
            }
        }
    }

    @Composable
    fun AboutScreen(navController: NavController, viewModel: GameViewModel) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Author: MockingB")
            Text(text = "${viewModel.state.value!!.topLevel.intValue}")
        }

    }

    @Composable
    fun GameScreen(navController: NavController) {
        if (viewModel.state.value!!.currentLevel.intValue == 0) viewModel.state.value!!.currentLevel.intValue =
            1
        val isEnabled = remember { mutableStateOf(true) }
        val gameOver = remember { mutableStateOf(viewModel.state.value!!.gameOver.value) }
        val currentLevel =
            remember { mutableIntStateOf(viewModel.state.value!!.currentLevel.intValue) }

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

        SideEffect {
            currentLevel.intValue = viewModel.state.value!!.currentLevel.intValue
        }

        SideEffect {
            gameOver.value = viewModel.state.value!!.gameOver.value
        }

        if (!gameOver.value && !viewModel.state.value!!.waitForUserInput && isEnabled.value) {
            isEnabled.value = false
            updateSharedPreferences(viewModel.state.value!!.topLevel.intValue)
            playSequence(
                viewModel = viewModel,
                interactionSource = interactionSource,
                randomSequence = viewModel.randomSequence.value!!,
                isEnabled
            )
        }

        if (gameOver.value) {
            viewModel.job.cancel()
            ShowGameOverDialog(viewModel.state.value!!.currentLevel.intValue, {
                viewModel.generateNewSequence()
                viewModel = ViewModelProvider(
                    this, GameViewModelFactory(this, soundPlayer, getTopLevelValue())
                )[GameViewModel::class.java]
                coroutineScope = viewModel.viewModelScope
                viewModel.state.value?.currentLevel?.intValue = 0
                isEnabled.value = true
            }) {
                navController.navigate("Home")
            }
        }

    }

    @Composable
    private fun FreeGameScreen(onBack: () -> Unit) {
        val isEnabled = remember { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(onClick = { onBack() }) { Text(text = "Back") }
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
    }

    private fun playSequence(
        viewModel: GameViewModel,
        interactionSource: List<MutableInteractionSource>,
        randomSequence: List<Int>,
        isEnabled: MutableState<Boolean>
    ) {
        coroutineScope.launch(viewModel.job) {
            for (buttonId in randomSequence) {
                delay(1000)
                val press = PressInteraction.Press(Offset.Zero)
                interactionSource[buttonId].emit(press)
                viewModel.onButtonClicked(buttonId)
                interactionSource[buttonId].emit(PressInteraction.Release(press))
            }
            delay(2000)
            isEnabled.value = true
            viewModel.setWaitForUserInput(true)
        }
    }

    @Composable
    fun ShowGameOverDialog(currentLevel: Int, onRestart: () -> Unit, onHome: () -> Unit) {
        var dialogVisible by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = {
                dialogVisible = false
                onRestart()
            },
            title = { Text("Game Over") },
            text = { Text("Your level: $currentLevel") },
            confirmButton = {
                Button(onClick = {
                    dialogVisible = false
                    onHome()
                }) {
                    Text("Home")
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
        LocalContext.current
        val navController = rememberNavController()
        RTSTheme {
            GameScreen(navController)
        }
    }
}
