package com.example.voicehelper

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.voicehelper.databinding.ActivityDialogQuestionAnswerBinding

class DialogQuestionAnswer (context: Context) : Dialog(context) {
    private var binding: ActivityDialogQuestionAnswerBinding = ActivityDialogQuestionAnswerBinding.inflate(layoutInflater)
    private var callBack : DialogCallBack? = null
    private var functionListVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        submitBtnListener()
        functionTextListener()
    }

    private fun functionTextListener() {
        binding.functionText.setOnClickListener {
            if (functionListVisible) {
                val animation = createAnimation({}, {binding.functionList.isGone = true}, R.anim.close_fuction_list)
                changeList(R.drawable.drop_down, animation)
            }
            else {
                 val animation = createAnimation({binding.functionList.isVisible = true}, {}, R.anim.open_function_list)
                changeList(R.drawable.drop_up, animation)
            }
            functionListVisible = !functionListVisible
        }
    }

    private fun createAnimation(openFunction: () -> Unit, closeFunction: () -> Unit, anim: Int): Animation {
        val loadAnimation = AnimationUtils.loadAnimation(context, anim)
        loadAnimation.setAnimationListener(object : AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {}

            override fun onAnimationStart(p0: Animation?) = openFunction()

            override fun onAnimationEnd(p0: Animation?) = closeFunction()
        })
        return loadAnimation
    }

    private fun changeList(picture: Int, loadAnimation: Animation) {
        val drawable = ContextCompat.getDrawable(context, picture)
        binding.functionText.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
        binding.functionList.startAnimation(loadAnimation)
    }

    private fun submitBtnListener() {
        binding.submitButton.setOnClickListener {
            if (checkText()) {
                callBack?.onDataEntered(binding.questionEditText.text.toString(), binding.answerEditText.text.toString())
                dismiss()
            }
            else binding.textViewReminder.isVisible = true
        }
    }
    fun setCallBack (callBack: DialogCallBack) {
        this.callBack = callBack
    }

    private fun checkText(): Boolean = !binding.answerEditText.text.isNullOrEmpty() &&
                !binding.questionEditText.text.isNullOrEmpty()
}

interface DialogCallBack {
    fun onDataEntered (question: String, answer: String)
}
