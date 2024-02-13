//GameViewModel.kt
package com.example.rts

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameViewModel(context: Context, private val soundPlayer: SoundPlayer) : ViewModel() {
    fun game() {
        viewModelScope.launch {

        }
    }

    private val gameModel = MutableLiveData(GameModel(context))
    private var _randomSequence: MutableLiveData<MutableList<Int>>? = null
    val randomSequence: LiveData<MutableList<Int>>
        get() {
            if (_randomSequence == null) _randomSequence =
                MutableLiveData(generateRandomSequence().toMutableList())
            return _randomSequence!!
        }

    fun setWaitForUserInput(value: Boolean) {
        gameModel.value?.setWaitForUserInput(value)
    }

    fun clearUserInput() {
        gameModel.value?.apply {
            userInput = mutableListOf()
        }
        gameModel.postValue(gameModel.value)
    }

    fun updateRandomSequence() {
        _randomSequence?.value?.add((0..3).random())
        _randomSequence?.postValue(_randomSequence?.value)
    }

    fun resetState() {
        state.value?.userInput?.clear()
        _randomSequence?.value?.clear()
        state.value?.currentLevel?.intValue = 1
    }

    val state: LiveData<GameModel>
        get() = gameModel

    fun onButtonClicked(buttonId: Int) {
        soundPlayer.playSound(SoundUtil.getSoundResource(buttonId))
        if (gameModel.value!!.userPlaying) {
            Log.d("ViewModel:", "Check user click")
            checkUserClick(buttonId)
            if(gameModel.value!!.userInput.size >= _randomSequence?.value?.size!!) {
                gameModel.value!!.currentLevel.intValue++
                gameModel.value!!.userPlaying = false
                gameModel.value?.userInput?.clear()
                updateRandomSequence()
            }
        }
    }

    private fun checkUserClick(buttonId: Int): Boolean {
        gameModel.value!!.userInput.add(buttonId)
        val id = gameModel.value!!.userInput.size - 1
        if (_randomSequence?.value?.get(id) != buttonId) {
            gameModel.value?.gameOver = true
            Log.d("Game Over", "Game over condition met")
            gameModel.postValue(gameModel.value)
            return false
        }
        return true
    }

    fun checkUserInput(): Boolean {
        Log.d("Check User Input", "No param")
        return gameModel.value?.userInput == gameModel.value?.randomSequence
    }

    fun checkUserInput(randomSequence: MutableList<Int>): Boolean {
        Log.d("userInput vs. randomSequence", "${randomSequence == gameModel.value?.userInput}")
        return randomSequence == gameModel.value?.userInput
    }

    fun generateRandomSequence(): List<Int> {
        return List(4) { Random.nextInt(0, 4) }
    }

    suspend fun pause(delay: Long) {
        withContext(Dispatchers.Default) {
            delay(delay)
        }
    }
}