package com.example.voicehelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.voicehelper.databinding.ActivityMainBinding
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var launcher : ActivityResultLauncher<Intent>
    private val wordLibrary = WordLibrary()
    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModel(binding)
        registerForActivityResult()
        viewModel.initTextToSpeech(context = this)
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
            wordLibrary.clear.any {it in text.lowercase()} -> {
                binding.imageView.setImageDrawable(null)
                return
            }
            "включи фонарик" in text.lowercase() -> {
                return
            }
            wordLibrary.greeting.any { it == text.lowercase()}  -> {
                viewModel.speak("Пока")
                sleep(500)
                finish()
                return
            }
            wordLibrary.music.any{music-> wordLibrary.play.any{play-> "$play $music" in text.lowercase()}} -> {
                viewModel.playMusic(context = this)
            }
            wordLibrary.stop.any { stop -> stop in text.lowercase() || wordLibrary.music.any{music->
                "$stop $music" in text.lowercase() } } -> {
                    viewModel.stopMusic()
                }
            else -> viewModel.speak("Извините, не знаю такой команды")

        }
    }

    fun onClickMicrophone(view: View) {
        launcher.launch(viewModel.intentFromMicrophone())
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMusic()
    }
}