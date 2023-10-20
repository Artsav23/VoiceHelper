package com.example.voicehelper

import android.os.Parcelable
import java.io.Serializable

data class QuestionAndAnswerDataClass(
    var question: String,
    var answer: String
) : Serializable
