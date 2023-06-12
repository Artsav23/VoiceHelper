package com.example.voicehelper

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.voicehelper.databinding.ActivityMainBinding
import java.lang.Thread.sleep
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var launcher : ActivityResultLauncher<Intent>
    private lateinit var textToSpeech: TextToSpeech
    private val wordLibrary = WordLibrary()
    private var mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerForActivityResult()
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(this) {
            if (it != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            }
        }
    }

    private fun registerForActivityResult() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val text = requireNotNull(it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
                commands(text[0])
            }
        }
    }

    private fun commands(text: String) {
        when {
            "печенье" in text.lowercase() -> {
                binding.imageView.setImageResource(R.drawable.cookie)
                return
            }
            wordLibrary.clear.any {it in text.lowercase()}-> {
                binding.imageView.setImageDrawable(null)
                return
            }
            "включи фонарик" in text.lowercase() -> {
                return
            }
            wordLibrary.greeting.any { it == text.lowercase()}  -> {
                textToSpeech.speak("Пока", TextToSpeech.QUEUE_FLUSH, null, null)
                sleep(500)
                finish()
                return
            }
            wordLibrary.music.any{music-> wordLibrary.play.any{play-> "$play $music" in text.lowercase()}} -> {
                playMusic()
            }
            else -> textToSpeech.speak("Извините, не знаю такой команды", TextToSpeech.QUEUE_FLUSH, null, null)

        }
    }

    private fun playMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sound0)
        mediaPlayer.start()
    }

    fun onClickMicrophone(view: View) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        launcher.launch(intent)
    }
}