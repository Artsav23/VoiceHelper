package com.example.voicehelper

import dalvik.annotation.TestTargetClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.Flow

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    infix fun Int.myPlus(y: Int): Int {
        return y * this
    }

    @Test
    fun mn() {
        val result = (3 myPlus 4) and 5
        println(result)
    }



    @Test
    fun chatGpt() {

        val apiKey = "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"
        val apiUrl = "https://api.openai.com/v1/chat/completions"

        val httpClient = OkHttpClient()

        val requestBody = """
        {
            "model": "gpt-3.5-turbo",
            "messages": [
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": "Когда родился лукашенко"}
            ]
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer $apiKey")
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful) {
            val responseJSON = JSONObject(responseBody)
            val answer = responseJSON.getJSONArray("choices").getJSONObject(0).getString("content")
            println(answer)
        } else {
            // Обработка ошибки при запросе к API
            println("Ошибка при выполнении запроса: ${response.code} - ${responseBody}")
        }
    }

    @Test
    fun minFun() = runBlocking {
            // Создаем простой flow, который отправляет числа от 1 до 5
            val myFlow = flow {
                for (i in 1..5) {
                    delay(200) // Имитация работы
                    emit(i)    // Отправляем значение в flow
                }
            }

            // Используем операторы для манипуляции данными в потоке
            myFlow
                .map { it * 2 }          // Умножаем каждое значение на 2
                .filter { it % 3 == 0 }  // Оставляем только значения, которые делятся на 3
                .transform { value ->

                    emit(value)
                    emit(value + 1)
                }
                .collect { value ->
                    println("Received: $value")
                }
    }
}