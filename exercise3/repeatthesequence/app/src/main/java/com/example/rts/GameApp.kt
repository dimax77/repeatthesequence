//GameScreen.kt
package com.example.rts

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rts.data.TopLevelRepository
import com.example.rts.ui.StartScreen
import com.example.rts.utils.GameViewModelFactory
import com.example.rts.utils.SoundPlayer

enum class GameScreen(@StringRes val title: Int) {
    Start(R.string.start),
    Game(R.string.game),
    Settings(R.string.settings),
    About(R.string.about)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAppBar(
    currentScreen: GameScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}


@Composable
fun GameApp() {
    val topLevelRepository = TopLevelRepository(LocalContext.current)
    val soundPlayer = SoundPlayer(LocalContext.current)
    val viewModel = GameViewModelFactory(topLevelRepository, soundPlayer)

    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen =
        GameScreen.valueOf(backStackEntry?.destination?.route ?: GameScreen.Start.name)

    Scaffold(topBar = {
        GameAppBar(
            currentScreen = currentScreen,
            canNavigateBack = navController.previousBackStackEntry != null,
            navigateUp = { navController.navigateUp() })
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = GameScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = GameScreen.Start.name) {
                StartScreen(
                    navigateToGame = { navController.navigate(GameScreen.Game.name) },
                    navigateToSettings = { navController.navigate(GameScreen.Settings.name) },
                    navigateToAbout = { navController.navigate(GameScreen.About.name) })
            }
            composable(route = GameScreen.Game.name) {}
            composable(route = GameScreen.Settings.name) {}
            composable(route = GameScreen.About.name) {}
        }
    }
}
