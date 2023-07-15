package com.example.voicehelper

const val API_URL = "https://api.openai.com/v1/chat/completions"
class WordLibrary {
    val greeting = listOf("привет", "добрый день", "приветик", "hello",
        "hi", "здравствуй")
    val clear = listOf("убери","очисти","убрать","очистить","стери","стереть")
    val play = listOf("включи", "врубай", "сыграй", "play", "включай")
    val music = listOf("music", "музыку", "песню", "песни", "мелодию", "мелодии")
    val stop = listOf("stop", "стоп", "пауза", "останови", "прекрати", "не надо", "выключи", "off")
    val gif = listOf("giff","giph","gif","giffs","gifs","гиф","гифф","гифы","гифку","гифка","гифки","гифы")
    val find = listOf("найди", "поиск", "поищи", "найти")
    val cookies = listOf("печенье", "печеньки", "печенька", "печеньку", "печеньи", "печенюга", "крекер", "крекеры")
    val flashLight = listOf("фонарь", "фонарик", "свет", "лампочка", "вспышка" )
}