package com.example.voicehelper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.example.voicehelper.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val wordLibrary = WordLibrary()
    private lateinit var viewModel: ViewModel
    private lateinit var handler: Handler
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var lineSoundView: LineSoundView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModel(binding)
        lineSoundView = LineSoundView(this, null)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        viewModel.initTextToSpeech(context = this)
        handler = viewModel.createHandler(this)
        requestMicrophonePermission()
        speechRecognizerListener()
        viewModel.animateText(binding.textView)
        createAnswer("напиши 100 слов на тему перФЕКЦИОНИЗМ")
    }

    private fun requestMicrophonePermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this, permission) != granted )
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun speechRecognizerListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                viewModel.animateTextStop()
                binding.textView.text = ""
                binding.inputText.isInvisible = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                binding.lineSound.startAnimation(rmsdB * 5)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                binding.lineSound.isInvisible = true
            }
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!text.isNullOrEmpty()) {
                    commands(text[0])
                    binding.textView.text = text[0].capitalize()
                }
                binding.lineSound.isInvisible = true
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialResult = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partialResult.isNullOrEmpty()) {
                    binding.textView.text = partialResult[0].capitalize()
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun commands(text: String) {
        when {
            wordLibrary.find.any { find -> find in text.lowercase() } -> {
                viewModel.speak("Вот что нашлось по вашему запросу")
                connectionWithNet(text.lowercase())
            }
            wordLibrary.cookies.any { cookies -> cookies in text.lowercase() } -> {
                binding.imageView.setImageResource(R.drawable.cookie)
                return
            }
            wordLibrary.clear.any {clear -> clear in text.lowercase()} -> {
                binding.imageView.setImageDrawable(null)
                return
            }
            wordLibrary.flashLight.any {flashLight -> wordLibrary.play.any {play ->
                "$play $flashLight" in text.lowercase()
            } }  -> viewModel.turnFlashLight(this, true)

            wordLibrary.greeting.any { greeting -> greeting == text.lowercase()}  -> {
                viewModel.speak("Пока")
                sleep(500)
                finish()
                return
            }
            wordLibrary.music.any{music -> wordLibrary.play.any{ play -> "$play $music" in text.lowercase()}} -> {
                viewModel.playMusic(context = this)
                binding.pauseMusic.isVisible = true
            }
            wordLibrary.stop.any { stop ->  wordLibrary.music.any{ music ->
                "$stop $music" in text.lowercase() } } -> {
                viewModel.stopMusic()
                binding.pauseMusic.isVisible = false
                }
            wordLibrary.stop.any { stop -> stop in text.lowercase() } -> {
                viewModel.turnFlashLight(this, false)
                viewModel.stopMusic()
                binding.pauseMusic.isVisible = false
            }

            wordLibrary.gif.any{gif -> gif in text.lowercase()} -> {
                showGif(text.lowercase())
            }
            else -> createAnswer(text)


        }
    }

    private fun showGif(text: String) {
        val  regex = Regex(wordLibrary.gif.joinToString(separator = "|", transform = Regex::escape))
        var search = text.replace(regex, "")
        if (search == "") search = "Gif"

        thread {
            val apiKey = resources.getString(R.string.API_KEY_GIF)
            val url = viewModel.createGif(search, apiKey)
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
        speechRecognizer.startListening(viewModel.intentFromMicrophone())
        binding.lineSound.isVisible = true
    }

    fun onClickPause(view: View) {
        viewModel.stopMusic()
        binding.pauseMusic.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMusic()
    }

    private fun createAnswer(text: String) {
        GlobalScope.launch {
            try {
                val httpClient = OkHttpClient.Builder()
                    .callTimeout(180, TimeUnit.SECONDS)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .writeTimeout(180, TimeUnit.SECONDS)
                    .build()
                val requestBody = """{ "model": "gpt-3.5-turbo", "messages": 
            [ {"role": "system", "content": "You are a helpful assistant."},
{"role": "user", "content": "$text"}]}""".trimIndent()
                val request = Request.Builder()
                    .url(API_URL)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .header("Authorization", "Bearer ${resources.getString(R.string.API_KEY_OPEN_AI)}")
                    .build()
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    val responseJSON = JSONObject(responseBody)
                    val answer = responseJSON.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    runOnUiThread {
                        binding.inputText.text = answer
                        Log.d("my_log", answer.toString())
                        binding.inputText.isVisible = true
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "Ошибка при выполнении запроса: ${response.code} - ${responseBody}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity,
                        "Что-то пошло не так, повторите пожалуйста запрос позже", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}