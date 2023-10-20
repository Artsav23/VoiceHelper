package com.example.voicehelper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voicehelper.databinding.ActivityItemBinding

private var answer = mutableListOf<QuestionAndAnswerDataClass>()

class AnswerAdapter: RecyclerView.Adapter<AnswerAdapter.ViewHolder>(), OnDeleteClickListener {

    class ViewHolder(item: View, private val listener: OnDeleteClickListener): RecyclerView.ViewHolder(item) {
        private val binding = ActivityItemBinding.bind(item)

        fun bind(position: Int) {
            binding.answer.text = "Answer: " + answer[position].answer
            binding.question.text = "Question: " + answer[position].question
            binding.deleteButton.setOnClickListener {
                listener.onDeleteClick(position)
            }
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
    fun getAnswerList(): MutableList<QuestionAndAnswerDataClass> {
        return answer
    }

    override fun onDeleteClick(position: Int) {
        answer.removeAt(position)
        notifyDataSetChanged()
    }
}
interface OnDeleteClickListener {
    fun onDeleteClick(position: Int)
}
