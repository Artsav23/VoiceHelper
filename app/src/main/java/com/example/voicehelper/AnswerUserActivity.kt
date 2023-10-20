package com.example.voicehelper

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voicehelper.databinding.ActivityAnswerUserBinding
import java.io.Serializable

class AnswerUserActivity : AppCompatActivity(), DialogCallBack {

    private lateinit var binding: ActivityAnswerUserBinding
    private val adapter = AnswerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                intent.putExtra("mutableList", adapter.getAnswerList() as Serializable)
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
}