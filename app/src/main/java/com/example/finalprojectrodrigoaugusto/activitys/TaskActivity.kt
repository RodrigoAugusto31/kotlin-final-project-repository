package com.example.finalprojectrodrigoaugusto.activitys

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.finalprojectrodrigoaugusto.MainActivity
import com.example.finalprojectrodrigoaugusto.R
import com.example.finalprojectrodrigoaugusto.util.TimePicker
import com.example.finalprojectrodrigoaugusto.databinding.ActivityTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class TaskActivity : AppCompatActivity() {

    private var _binding: ActivityTaskBinding? = null
    private val binding get() = _binding!!

    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks")

    private var taskId: String = ""

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
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks/$taskId")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()) return
                    val task = snapshot.value as HashMap<String, String>

                    task["titulo"] = binding.tasksEdiTxtTitle.text.toString()
                    task["descricao"] = binding.tasksEdiTxtDescription.text.toString()
                    task["data"] = binding.tasksEditTxtDate.text.toString()
                    task["hora"] = binding.tasksEditTxtHour.text.toString()

                    ref.setValue(task)
                    Toast.makeText(this@TaskActivity, "Tarefa atualizada com sucesso", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TaskActivity, "Erro ao atualizar tarefa", Toast.LENGTH_SHORT).show()
                }

            })
            backToHomeScreen()

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

            backToHomeScreen()
        }
    }

    private fun loadTask() {
        this.taskId = intent.getStringExtra("id") ?: ""
        if(taskId === "") return

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks/$taskId")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) return

                binding.tasksEdiTxtTitle.setText(snapshot.child("titulo").value.toString())
                binding.tasksEdiTxtDescription.setText(snapshot.child("descricao").value.toString())
                binding.tasksEditTxtDate.setText(snapshot.child("data").value.toString())
                binding.tasksEditTxtHour.setText(snapshot.child("hora").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TaskActivity, "Erro ao carregar tarefa", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setSaveTaskButtonListener() {
        binding.tasksBtSaveTask.setOnClickListener {
            createUpdateTask()
        }
    }

    private fun backToHomeScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}