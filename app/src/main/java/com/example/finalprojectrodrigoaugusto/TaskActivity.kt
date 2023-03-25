package com.example.finalprojectrodrigoaugusto

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.finalprojectrodrigoaugusto.databinding.ActivityMainBinding
import com.example.finalprojectrodrigoaugusto.databinding.ActivityTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class TaskActivity : AppCompatActivity() {

    private var _binding: ActivityTaskBinding? = null
    private val binding get() = _binding!!

    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks")

    var taskId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        loadTask()
        setUpTimeAndDate()
        setSaveTaskButtonListener()
    }

    private fun handleTimePickerVisibility() {
        binding.timePicker.root.visibility = View.VISIBLE
        binding.timePicker.timepickerLayout.visibility = View.VISIBLE
    }

    private fun setUpTimeAndDate() {

        val date = binding.tasksEditTxtDate
        val time = binding.tasksEditTxtHour

        val currentDateAndTime = Calendar.getInstance()
        val day = currentDateAndTime.get(Calendar.DAY_OF_MONTH)
        val month = currentDateAndTime.get(Calendar.MONTH)
        val year = currentDateAndTime.get(Calendar.YEAR)
        val hour = currentDateAndTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentDateAndTime.get(Calendar.MINUTE)

        date.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
        time.setText(String.format("%02d:%02d", hour, minute))

        binding.tasksBtDate.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(this, { _, yearOfYear, monthOfYear, dayOfMonth ->
                    date.setText(
                        String.format(
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            monthOfYear + 1,
                            yearOfYear
                        )
                    )
                }, year, month, day)
            datePickerDialog.show()
        }

        binding.tasksBtHour.setOnClickListener {

            handleTimePickerVisibility()

            TimePicker(this, binding.timePicker.timepicker).showTimePicker(
                binding.tasksEditTxtHour,
                binding.timePicker.timepickerBtSave,
                binding.timePicker.timepickerBtCancel,
                binding.timePicker.timepickerLayout

            )
        }
    }

    private fun createUpdateTask() {
        if (taskId !== "") {
//            TODO: Atualizar tarefa
        } else {
            val title = binding.tasksEdiTxtTitle
            val description = binding.tasksEdiTxtDescription
            val data = binding.tasksEditTxtDate
            val hour = binding.tasksEditTxtHour

            val task = hashMapOf(
                "titulo" to title.text.toString(),
                "descricao" to description.text.toString(),
                "data" to data.text.toString(),
                "hora" to hour.text.toString(),
            )

            val newElement = dbRef.push()
            newElement.setValue(task)

            Toast.makeText(this, getString(R.string.task_created), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTask() {
        this.taskId = intent.getStringExtra("id") ?: ""
        if (taskId === "") return
//        TODO: Carregar tarefa
    }

    private fun setSaveTaskButtonListener() {
        binding.tasksBtSaveTask.setOnClickListener {
            createUpdateTask()
        }
    }

}