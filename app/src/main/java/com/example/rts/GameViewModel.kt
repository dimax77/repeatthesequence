//GameViewModel.kt
package com.example.rts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameViewModel(private val soundPlayer: SoundPlayer) : ViewModel() {
    private val gameModel = MutableLiveData(GameModel())

    val state: LiveData<GameModel>
        get() = gameModel

    fun onButtonClicked(buttonId: Int) {
        soundPlayer.playSound(SoundUtil.getSoundResource(buttonId))
        if (gameModel.value!!.userPlaying) {
            gameModel.value!!.userInput.add(buttonId)
        }
    }

    fun checkUserInput(randomSequence: MutableList<Int>): Boolean {
        return randomSequence == gameModel.value!!.userInput
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