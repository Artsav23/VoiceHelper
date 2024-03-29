package com.example.voicehelper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.android.volley.toolbox.Volley
import com.example.voicehelper.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel = ViewModel()
    private lateinit var handler: Handler
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val wordLibrary = WordLibrary()
    private var speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    private var stoppedListen = false
    private var listCommand = mutableListOf<QuestionAndAnswerDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        viewModel.initTextToSpeech(context = this)
        handler = viewModel.createHandler(this, binding.imageView)
        requestMicrophonePermission()
        speechRecognizerListener()
        registerForActivityResultLauncher()
        viewModel.animateText(binding.textView, "Hello, user \nCan I help you?")
        listCommand = loadData()
    }

    private fun registerForActivityResultLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                listCommand = it.data?.getSerializableExtra("mutableList") as MutableList<QuestionAndAnswerDataClass>
            }
        }
    }

    private fun requestMicrophonePermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this, permission) != granted )
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                takeMessage("Разрешите доступ к микрофону")
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
                binding.lineSound.startAnimation(rmsdB)
                binding.buttonSoundView.startAnimation(rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { animateVisibleFalse() }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!text.isNullOrEmpty()) {
                    commands(text[0])
                    binding.textView.text = text[0].capitalize()
                }
                animateVisibleFalse()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialResult = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partialResult.isNullOrEmpty())  binding.textView.text = partialResult[0].capitalize()

            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun animateVisibleFalse() {
        binding.lineSound.isInvisible = true
        binding.buttonSoundView.isInvisible = true
        stoppedListen = false
    }

    private fun commands(text: String) {
        if (stoppedListen) {
            stoppedListen = false
            wordLibrary.apply {
                when {
                    answerUserQuestion(text = text) -> return
                    find.any { find -> find in text.lowercase() } -> findWithGoogle(text)

                    cookies.any { cookies -> cookies in text.lowercase() } -> binding.imageView.setImageResource(R.drawable.cookie)

                    clear.any { clear -> clear in text.lowercase() } -> binding.imageView.setImageDrawable(null)

                    flashLight.any { flashLight -> wordLibrary.play.any { play -> "$play " +
                            flashLight in text.lowercase() } }  -> viewModel.turnFlashLight(this@MainActivity, true)

                    greeting.any { greeting -> greeting == text.lowercase() }  -> bye()

                    music.any { music -> wordLibrary.play.any { play -> "$play $music" in text.lowercase() }} ->
                        viewModel.playMusic(context = this@MainActivity, binding.pauseMusic)

                    stop.any { stop -> stop in text.lowercase() } ->
                        viewModel.stopAll(this@MainActivity, binding.pauseMusic)

                    gif.any { gif -> gif in text.lowercase() } -> showGif(text.lowercase())

                    weather.any { weather -> weather in text.lowercase() } -> weather(text.lowercase())

                    open.any { open -> youtube.any { youtube -> "$open $youtube" in text.lowercase()} }
                    -> viewModel.openApp(this@MainActivity, "https://www.youtube.com")

                    open.any { open -> tiktok.any { tiktok -> "$open $tiktok" in text.lowercase()} }
                    -> viewModel.openApp(this@MainActivity, "https://www.tiktok.com")

                    open.any { open -> browser.any { browser -> "$open $browser" in text.lowercase()} }
                    -> viewModel.openApp(this@MainActivity, "https://www.google.com")

                    open.any { open -> settings.any { settings -> "$open $settings" in text.lowercase()} }
                    -> startActivity(Intent(Settings.ACTION_SETTINGS))

                    else -> getAnswerInOpenAI(text)
                }
            }
        }
        else {
            stoppedListen = true
        }
    }

    private fun answerUserQuestion(text: String): Boolean {
        var flag = false
            for (i in 0 until listCommand.count()) {
                if (text.lowercase() == listCommand[i].question.trim().lowercase() ||
                    ("$text.").lowercase() == listCommand[i].question.trim().lowercase()) {
                    flag = true
                    viewModel.speak(listCommand[i].answer)
                    binding.inputText.isVisible = true
                    binding.inputText.text = listCommand[i].answer
                }
            }
        return flag
    }

    private fun findWithGoogle(text: String) {
        viewModel.speak("Вот что нашлось по вашему запросу")
        connectionWithNet(text.lowercase())
    }

    private fun bye() {
        viewModel.speak("Пока")
        sleep(600)
        finish()

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
            takeMessage("Internet connection error")
        }
    }

    fun onClickMicrophone(view: View) {
        if (!stoppedListen) {
            stoppedListen = true
            speechRecognizer.startListening(viewModel.intentFromMicrophone())
        }
        else {
            stoppedListen = false
            speechRecognizer.stopListening()
        }
        viewModel.stopSpeak()
        viewModel.stopMusic()
        binding.lineSound.isVisible = true
        binding.buttonSoundView.isVisible = true

    }

    fun onClickPause(view: View) {
        binding.pauseMusic.isVisible = false
        viewModel.stopMusic()
    }
    fun onClickAddAnswer(view: View) = launcher.launch(Intent(this, AnswerUserActivity::class.java))


    private fun getAnswerInOpenAI(text: String) {
        binding.inputText.isVisible = true
        binding.inputText.text = "Writing..."
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val answer = viewModel.createAnswer(text, resources.getString(R.string.API_KEY_OPEN_AI))
            if (answer != "false")
            {
                withContext(Dispatchers.Main) {
                    viewModel.speak(answer)
                    binding.inputText.text = answer
                    binding.inputText.isVisible = true
                    viewModel.animateText(binding.inputText, answer)
                }
            }
            else {

                withContext(Dispatchers.Main) {
                    binding.inputText.text = ""
                    takeMessage("Что-то пошло не так, повторите пожалуйста запрос позже")
                }
            }
        }
    }

    private fun weather(text: String) {
        val queue = Volley.newRequestQueue(this)
        queue.add(viewModel.getWeather(text,resources.getString(R.string.API_KEY_WEATHER), binding.inputText))
    }

    private fun takeMessage(text: String) = Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()


    override fun onPause() {
        super.onPause()
        viewModel.stopAll(this, binding.pauseMusic)
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        viewModel.stopAll(this, binding.pauseMusic)
    }
    private fun loadData(): MutableList<QuestionAndAnswerDataClass> {
        val gson = Gson()
        val sharedPreferences = this.getSharedPreferences("QuestionAndAnswer", Context.MODE_PRIVATE)
        val type = object : TypeToken<MutableList<QuestionAndAnswerDataClass>>() {}.type
        val json = sharedPreferences.getString("QA", null)
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}