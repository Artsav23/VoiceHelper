package com.example.voicehelper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.voicehelper.databinding.ActivityItemBinding

private var answer = mutableListOf<QuestionAndAnswerDataClass>()

class AnswerAdapter: RecyclerView.Adapter<AnswerAdapter.ViewHolder>(), OnDeleteClickListener {

    class ViewHolder(item: View, private val listener: OnDeleteClickListener): RecyclerView.ViewHolder(item) {
        private val binding = ActivityItemBinding.bind(item)

        fun bind(position: Int) {
            binding.answer.text = "Answer: " + answer[position].answer
            binding.question.text = "Question: " + answer[position].question
            binding.deleteButton.setOnClickListener { setRemoveAnimator(position) }
        }
        private fun setRemoveAnimator(position: Int) {
            val loadAnimation = AnimationUtils.loadAnimation(itemView.context, R.anim.slide_out_left)
            loadAnimation.setAnimationListener(object : AnimationListener{
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    listener.onDeleteClick(position)
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
            itemView.startAnimation(loadAnimation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.activity_item, parent, false)
        return ViewHolder(inflater, this)
    }

    override fun getItemCount(): Int {
        return answer.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
    fun add (questionAndAnswer: QuestionAndAnswerDataClass) {
        answer.add(questionAndAnswer)
        notifyDataSetChanged()
    }
    fun addAll(questionAndAnswer: MutableList<QuestionAndAnswerDataClass>) {
        questionAndAnswer.forEach {
            answer.add(it)
        }
        notifyDataSetChanged()
    }
    fun getList(): MutableList<QuestionAndAnswerDataClass> {
        return answer
    }

    override fun onDeleteClick(position: Int) {
        answer.removeAt(position)
        notifyDataSetChanged()
    }

    fun clear() {
        answer.clear()
    }
}
interface OnDeleteClickListener {
    fun onDeleteClick(position: Int)
}
