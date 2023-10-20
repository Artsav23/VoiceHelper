package com.example.voicehelper

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.voicehelper.databinding.ActivityDialogQuestionAnswerBinding

class DialogQuestionAnswer (context: Context) : Dialog(context) {
    private var binding: ActivityDialogQuestionAnswerBinding = ActivityDialogQuestionAnswerBinding.inflate(layoutInflater)
    private var callBack : DialogCallBack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.submitButton.setOnClickListener {
            if (checkText()) {
                callBack?.onDataEntered(binding.questionEditText.text.toString(), binding.answerEditText.text.toString())
                dismiss()
            }
            else
                binding.textViewReminder.isVisible = true
        }
    }

    fun setCallBack (callBack: DialogCallBack) {
        this.callBack = callBack
    }

    private fun checkText(): Boolean {
        return !binding.answerEditText.text.isNullOrEmpty() &&
                !binding.questionEditText.text.isNullOrEmpty()
    }
}

interface DialogCallBack {
    fun onDataEntered (question: String, answer: String)
}
