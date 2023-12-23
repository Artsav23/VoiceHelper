package com.example.voicehelper

import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voicehelper.databinding.ActivityAnswerUserBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

class AnswerUserActivity : AppCompatActivity(), DialogCallBack {

    private lateinit var binding: ActivityAnswerUserBinding
    private val adapter = AnswerAdapter()
    private var mutableListDataClass = mutableListOf<QuestionAndAnswerDataClass>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadData()
        binding.rcView.adapter = adapter
        binding.rcView.layoutManager = LinearLayoutManager(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> showQuestionAnswerDialog()
            android.R.id.home -> {
                val intent = Intent()
                mutableListDataClass = adapter.getList()
                saveData()
                intent.putExtra("mutableList", mutableListDataClass as Serializable)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.toolbar.title = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.toolbar.navigationIcon?.colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN)
        } else {
            binding.toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
        menuInflater.inflate(R.menu.menu_add_answer, menu)
        return true
    }

    private fun showQuestionAnswerDialog() {
        val dialog = DialogQuestionAnswer(this)
        dialog.create()
        dialog.setCallBack(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = WindowManager.LayoutParams()
        dialog.show()
    }

    override fun onDataEntered(question: String, answer: String) {
        val questionAndAnswer = QuestionAndAnswerDataClass(question = question, answer = answer)
        adapter.add(questionAndAnswer)
    }

    override fun onPause() {
        super.onPause()
        saveData()
    }

    private fun saveData() {
        val gson = Gson()
        mutableListDataClass = adapter.getList()
        val json = gson.toJson(mutableListDataClass)
        val sharedPreferences = this.getSharedPreferences("QuestionAndAnswer", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("QA", json).apply()
    }

    private fun loadData() {
        val gson = Gson()
        val sharedPreferences = this.getSharedPreferences("QuestionAndAnswer", Context.MODE_PRIVATE)
        val type = object : TypeToken<MutableList<QuestionAndAnswerDataClass>>() {}.type
        val json = sharedPreferences.getString("QA", null)
        adapter.clear()
        mutableListDataClass = gson.fromJson(json, type) ?: mutableListOf()
        mutableListDataClass
        adapter.addAll(mutableListDataClass)
    }
}

