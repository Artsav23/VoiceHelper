package com.example.voicehelper

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.voicehelper.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.TimeUnit

class ViewModel {
    private var mediaPlayer = MediaPlayer()
    private lateinit var textToSpeech: TextToSpeech
    private val text = SpannableString("Hello, user \nCan I help you?")
    private lateinit var animator: ValueAnimator

    fun intentFromMicrophone(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        return intent
    }

    fun animateText(textView: TextView, text: String) {
        val txt = SpannableString(text)
        animator = ObjectAnimator.ofInt(0, txt.length)
        txt.setSpan(StyleSpan(Typeface.BOLD), 19, text.length, 0)
        animator.duration = 2000
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            textView.text = txt.subSequence(0, animatedValue)
        }
        animator.start()
    }


    fun animateTextStop() {
        animator.cancel()
    }

    fun playMusic(context: Context) {
        val musicList = arrayListOf(R.raw.sound0, R.raw.sound1, R.raw.sound2, R.raw.sound3, R.raw.sound4)
        mediaPlayer = MediaPlayer.create(context, musicList.random())
        mediaPlayer.start()
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
    fun stopSpeak() {
        textToSpeech.stop()
    }

    fun stopMusic() {
        mediaPlayer.stop()
    }

    fun stopAll(context: Context) {
        turnFlashLight(context, false)
        stopMusic()
    }

    fun searchWithInternet(text: String): Intent {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, text)
        return intent
    }
    fun createGif(text: String, apiKey: String) : String {
        val jsonString = response(text, apiKey)
        val jsonObject = JSONObject(jsonString)
        val url = jsonObject.getJSONArray("data")
            .getJSONObject(0)
            .getJSONObject("images")
            .getJSONObject("original")
            .getString("url")
        return url
    }

    private fun response(text: String, apiKey: String): String {
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

    fun createHandler(context: Context, imageView: ImageView): android.os.Handler {
        val handler = android.os.Handler(Looper.getMainLooper()) { message ->
            val data = message.obj as String
            try {
                Glide.with(context).load(data).into(imageView)
            } catch (e: Exception) {
                Toast.makeText(context, "По вашему запросу ничего не было найдено", Toast.LENGTH_SHORT).show()
            }
            true
        }
        return handler
    }

    fun searchWithInternetCompat(text: String): Intent {
        val uri = Uri.parse("http://www.google.com/#q=$text")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    fun turnFlashLight(context: Context, status: Boolean) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        cameraManager.setTorchMode(cameraId, status)
    }
    fun createAnswer(text: String, apiKeyOpenAI: String, binding: ActivityMainBinding): String {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()
        val requestBody = """{ "model": "gpt-3.5-turbo", "messages": [ {"role": "system", "content":
                     "You are a helpful assistant."},{"role": "user", "content": "$text"}]}""".trimIndent()
        val request = Request.Builder()
            .url(API_URL_OPEN_AI).post(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer $apiKeyOpenAI")
            .build()
        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string()

            try {
                if (response.isSuccessful) {
                    val responseJSON = JSONObject(responseBody)
                    val answer = responseJSON.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    return answer
                } else {
                    return "false"
                }
            } catch (e: Exception) {
                return "false"
            }
    }

    fun getCity(text: String): String {
        var city = ""
        WordLibrary().weather.forEach {
            if (it in text)
            city = text.replace("$it ", "")
        }
        return city
    }

    fun openApp(context: Context, uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun getWeather(text: String, apiKey: String, inputText: TextView): StringRequest {
        val city = getCity(text)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=${apiKey}&units=metric"
        val stringRequest = StringRequest(
            com.android.volley.Request.Method.POST, url,
            { response ->
                val resultTemp = JSONObject(response).getJSONObject("main").getString("temp")
                val resultWeather = JSONObject(response).getJSONArray("weather").getJSONObject(0).getString("description")
                inputText.text = "Tempeture: $resultTemp. Weather: $resultWeather."
                inputText.isVisible = true
                speak("Температура $resultTemp градуса. Одевайтесь по погоде.")
            },
            { speak("Sorry, there was an error, repeat later")})
        return  stringRequest
    }
}