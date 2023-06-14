package com.example.voicehelper

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.core.view.isVisible
import com.example.voicehelper.databinding.ActivityMainBinding
import java.util.Locale

class ViewModel(private val binding: ActivityMainBinding) {
    private var mediaPlayer = MediaPlayer()
    private lateinit var textToSpeech: TextToSpeech

    fun intentFromMicrophone(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        return intent
    }

    fun playMusic(context: Context) {
        val musicList = arrayListOf(R.raw.sound0, R.raw.sound1, R.raw.sound2, R.raw.sound3, R.raw.sound4)
        mediaPlayer = MediaPlayer.create(context, musicList.random())
        mediaPlayer.start()
        binding.pauseMusic.isVisible = true
    }

    fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) {
            if (it != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            }
        }
    }

    fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stopMusic() {
        mediaPlayer.stop()
    }
}