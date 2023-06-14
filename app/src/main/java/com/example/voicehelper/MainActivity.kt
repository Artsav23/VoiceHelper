package com.example.voicehelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.voicehelper.databinding.ActivityMainBinding
import java.lang.Exception
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
                binding.pauseMusic.isVisible = true
            }
            wordLibrary.stop.any { stop -> stop in text.lowercase() || wordLibrary.music.any{music->
                "$stop $music" in text.lowercase() } } -> {
                viewModel.stopMusic()
                binding.pauseMusic.isVisible = false
                }
            else -> {
                viewModel.speak("Вот что удалось найти по вашему запросу")
               connectionWithNet(text = text)
            }

        }
    }

    private fun connectionWithNet(text: String) {
        try {
            startActivity(viewModel.searchWithInternet(text))
        } catch (e: Exception) {
            connectionWithNetCompat(text = text)
        }
    }

    private fun connectionWithNetCompat(text: String) {
        try {
            startActivity(viewModel.searchWithInternetCompat(text))
        }
        catch (e: Exception) {
            Toast.makeText(this, "Internet connection error", Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickMicrophone(view: View) {
        launcher.launch(viewModel.intentFromMicrophone())
    }

    fun onClickPause(view: View) {
        viewModel.stopMusic()
        binding.pauseMusic.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMusic()
    }
}