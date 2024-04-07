//GameViewModel.kt

package com.example.rts.ui

import android.content.SharedPreferences
import android.content.Context
import android.util.Log
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.rts.data.GameModel
import com.example.rts.data.GameUtils
import com.example.rts.data.TopLevelRepository
import com.example.rts.utils.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class GameViewModel(
    private val topLevelRepository: TopLevelRepository,
    private val soundPlayer: SoundPlayer,
    applicationContext: Context
) : ViewModel() {


    private var _gameState =
        MutableStateFlow(GameModel(topLevelRepository.getTopLevel(), applicationContext))
    val gameState = _gameState.asStateFlow()


    private fun updateTopLevel() {
        if (_gameState.value.topLevel < _gameState.value.currentLevel.intValue) {
            _gameState.value.topLevel = _gameState.value.currentLevel.intValue
            topLevelRepository.saveTopLevel(gameState.value.currentLevel.intValue)
        }
    }

    fun onButtonClicked(buttonId: Int) {
        soundPlayer.playSound(GameUtils.getSoundResource(buttonId))
        if (_gameState.value.waitForUserInput.value) {
            if (!userClickOk(buttonId)) {
                _gameState.value.waitForUserInput.value = false
                _gameState.value.gameOver.value = true
            } else
                if (sequenceComplete()) {
//                _gameState.value.userPlaying.value = false
                    _gameState.value.userInput.clear()
                    updateRandomSequence()
                    _gameState.value.waitForUserInput.value = false
                    _gameState.value.currentLevel.intValue++
                    updateTopLevel()
                }
        }
    }

    private fun userClickOk(buttonId: Int): Boolean {
        _gameState.value.userInput.add(buttonId)
        val id = _gameState.value.userInput.size - 1
        return _gameState.value.randomSequence?.value?.get(id) == buttonId
    }
//
//    private var _job = Job()
//        get() {
//            if (field.isCancelled) field = Job()
//            return field
//        }
//
//    fun cancelAndRestartJob() {
//        _job.cancel()
//        _job = Job()
//    }

    fun setWaitForUserInput() {
        _gameState.value.waitForUserInput.value = true
    }

    private fun sequenceComplete(): Boolean {
        return _gameState.value.userInput.size >= _gameState.value.randomSequence?.value?.size!!
    }
    private fun generateNewSequence() {
        _gameState.value.randomSequence?.value = generateRandomSequence().toMutableList()
    }

    private fun updateRandomSequence() {
        _gameState.value.randomSequence?.value?.add((0..3).random())
        _gameState.value.randomSequence?.postValue(_gameState.value.randomSequence?.value)
    }

    private fun generateRandomSequence(): List<Int> {
        return List(4) { Random.nextInt(0, 4) }
    }

    val randomSequence: LiveData<List<Int>>
        get() {
            if (_gameState.value.randomSequence?.value == null) _gameState.value.randomSequence =
                MutableLiveData(generateRandomSequence().toMutableList())
            return _gameState.value.randomSequence!!.map { it.toList() }
        }

    fun resetState() {
        generateNewSequence()
        _gameState.value.userInput.clear()
        _gameState.value.currentLevel.intValue = 1
        _gameState.value.gameOver.value = false
        _gameState.value.waitForUserInput.value = false
    }


}