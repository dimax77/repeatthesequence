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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameViewModel(context: Context, private val soundPlayer: SoundPlayer, topLevelValue: Int) :
    ViewModel() {

    private val gameModel = MutableLiveData(GameModel(context, topLevelValue))
    private var _randomSequence: MutableLiveData<MutableList<Int>>? = null

    val randomSequence: LiveData<MutableList<Int>>
        get() {
            if (_randomSequence == null) _randomSequence =
                MutableLiveData(generateRandomSequence().toMutableList())
            return _randomSequence!!
        }

    private var _job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }
    val job: Job
        get() {
            return _job
        }

    fun setWaitForUserInput(value: Boolean) {
        gameModel.value?.setWaitForUserInput(value)
    }

    private fun updateRandomSequence() {
        _randomSequence?.value?.add((0..3).random())
        _randomSequence?.postValue(_randomSequence?.value)
    }


    fun generateNewSequence() {
        _randomSequence?.value = generateRandomSequence().toMutableList()
        Log.d("viewModel.generateNewSequence", "New sequence generated")
    }

    fun resetState() {
        state.value?.userInput?.clear()
        state.value?.gameOver?.value = false
    }

    val state: LiveData<GameModel>
        get() = gameModel

    fun onButtonClicked(buttonId: Int) {
        soundPlayer.playSound(SoundUtil.getSoundResource(buttonId))
        if (gameModel.value!!.waitForUserInput) {
            checkUserClick(buttonId)
            if (gameModel.value!!.userInput.size >= _randomSequence?.value?.size!!) {
                Log.d("viewModel.onButtonClicked", "Level updated")
                gameModel.value!!.userPlaying.value = false
                gameModel.value?.userInput?.clear()
                updateRandomSequence()
                gameModel.value!!.setWaitForUserInput(value = false)
                gameModel.value!!.currentLevel.intValue++
                if (gameModel.value!!.topLevel.intValue < gameModel.value!!.currentLevel.intValue)
                    gameModel.value!!.topLevel.intValue = gameModel.value!!.currentLevel.intValue
            }
        }
    }

    private fun checkUserClick(buttonId: Int) {
        gameModel.value!!.userInput.add(buttonId)
        val id = gameModel.value!!.userInput.size - 1
        if (_randomSequence?.value?.get(id) != buttonId) {
            gameModel.value!!.setWaitForUserInput(false)
            gameModel.value!!.gameOver?.value = true
            Log.d("viewModel.checkUserClick", "Game Over")
        }
    }

    private fun generateRandomSequence(): List<Int> {
        return List(4) { Random.nextInt(0, 4) }
    }

}