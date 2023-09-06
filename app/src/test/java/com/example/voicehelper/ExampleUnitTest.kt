package com.example.voicehelper

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*

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
    fun minFun() {
        val input = readLine()!!.split(" ").map { it.toInt() }
        val n = input[0]
        val t = input[1]

        var minJoy = Int.MAX_VALUE

        repeat(n) {
            val k = readLine()!!.toInt()
            val gifts = readLine()!!.split(" ").map { it.toInt() }

            val sortedGifts = gifts.sorted()

            var totalCost = 0
            var totalJoy = 0

            for (i in 0 until k) {
                if (totalCost + sortedGifts[i] <= t) {
                    totalCost += sortedGifts[i]
                    totalJoy += sortedGifts[i]
                } else {
                    break
                }
            }

            minJoy = minOf(minJoy, totalJoy)
        }

        println(minJoy)
    }
}