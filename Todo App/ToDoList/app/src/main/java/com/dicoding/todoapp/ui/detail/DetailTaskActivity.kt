package com.dicoding.todoapp.ui.detail

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.todoapp.R
import com.dicoding.todoapp.ui.ViewModelFactory
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.TASK_ID
import com.google.android.material.textfield.TextInputEditText

class DetailTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        //TODO 11 : Show detail task and implement delete action

        val detailTaskViewModel: DetailTaskViewModel
        val factory = ViewModelFactory.getInstance(this)
        detailTaskViewModel = ViewModelProvider(this, factory)[DetailTaskViewModel::class.java]

        if (intent.hasExtra(TASK_ID)) {
            val taskId = intent.getIntExtra(TASK_ID, -1)

            detailTaskViewModel.apply {
                setTaskId(taskId)
                task.observe(this@DetailTaskActivity) { task ->
                    if (task == null) {
                        finish()
                    } else {
                        findViewById<TextInputEditText>(R.id.detail_ed_title).setText(task.title)
                        findViewById<TextInputEditText>(R.id.detail_ed_due_date).setText(
                            DateConverter.convertMillisToString(
                                task.dueDateMillis
                            )
                        )
                        findViewById<TextInputEditText>(R.id.detail_ed_description).setText(task.description)

                        findViewById<Button>(R.id.btn_delete_task).setOnClickListener {
                            deleteTask()
                            finish()
                        }
                    }
                }
            }
        } else {
            finish()
        }
    }
}