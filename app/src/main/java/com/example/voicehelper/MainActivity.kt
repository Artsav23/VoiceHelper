package com.example.voicehelper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.voicehelper.databinding.ActivityMainBinding
import org.json.JSONObject
import java.lang.Thread.sleep
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var launcher : ActivityResultLauncher<Intent>
    private val wordLibrary = WordLibrary()
    private lateinit var viewModel: ViewModel
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModel(binding)
        registerForActivityResult()
        viewModel.initTextToSpeech(context = this)
        handler = viewModel.createHandler(this)
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
            wordLibrary.find.any { it in text.lowercase() } -> {
                viewModel.speak("Вот что нашлось по вашему запросу")
                connectionWithNet(text.lowercase())
            }
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
            wordLibrary.gif.any{gif -> gif in text.lowercase()} -> {
                showGif(text.lowercase())
            }
            else -> viewModel.speak("Не знаю такой команды")


        }
    }

    private fun showGif(text: String) {
        val  regex = Regex(wordLibrary.gif.joinToString(separator = "|", transform = Regex::escape))
        var search = text.replace(regex, "")
        if (search == "") search = "Gif"

        thread { val url = viewModel.createGif(search)
            val message = Message.obtain()
            message.obj = url
            handler.sendMessage(message)
        }

    }

    private fun connectionWithNet(text: String) {
        val regex = Regex(wordLibrary.find.joinToString( separator = "|", transform = Regex::escape))
        val search = text.replace(regex,"")
        try {
            startActivity(viewModel.searchWithInternet(search))
        } catch (e: Exception) {
            connectionWithNetCompat(search)
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