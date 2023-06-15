package com.example.voicehelper

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.voicehelper.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.logging.Handler
import kotlin.concurrent.thread

class ViewModel(private val binding: ActivityMainBinding) {
    private var mediaPlayer = MediaPlayer()
    private lateinit var textToSpeech: TextToSpeech
    private val apiKey = "JnoqxlzqBsM6e41fpdhzDnJ1qAPzsuwq"

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

    fun searchWithInternet(text: String): Intent {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, text)
        return intent
    }
    fun createGif(text: String) : String {
        val jsonString = response(text)
        val jsonObject = JSONObject(jsonString)
        val url = jsonObject.getJSONArray("data")
            .getJSONObject(0)
            .getJSONObject("images")
            .getJSONObject("original")
            .getString("url")
        return url
    }

    private fun response(text: String): String {
        val url = URL("https://api.giphy.com/v1/gifs/search?api_key=$apiKey&q=$text")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val data: String
        try {
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            data = response.toString()
        } finally {
            connection.disconnect()
        }
        return data
    }

    fun createHandler(context: Context): android.os.Handler {
        val handler = android.os.Handler(Looper.getMainLooper()) { message ->
            val data = message.obj as String
            try {

                Glide.with(context).load(data).into(binding.imageView)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "По вашему запросу ничего не было найдено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
        return handler
    }

    fun searchWithInternetCompat(text: String): Intent {
        val uri = Uri.parse("http://www.google.com/#q=$text")
        return Intent(Intent.ACTION_VIEW, uri)
    }
}