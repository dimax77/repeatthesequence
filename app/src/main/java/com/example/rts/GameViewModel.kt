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
    private val gameModel = MutableLiveData(GameModel(context))
    private var _randomSequence: MutableLiveData<MutableList<Int>>? = null
    val randomSequence: LiveData<MutableList<Int>>
        get() {
            if (_randomSequence == null) _randomSequence =
                MutableLiveData(generateRandomSequence().toMutableList())
            return _randomSequence!!
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
            gameModel.value!!.userInput.add(buttonId)
        }
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