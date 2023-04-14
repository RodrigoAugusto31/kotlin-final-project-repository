package com.example.finalprojectrodrigoaugusto.activitys

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectrodrigoaugusto.MainActivity
import com.example.finalprojectrodrigoaugusto.R
import com.example.finalprojectrodrigoaugusto.databinding.ActivityTaskBinding
import com.example.finalprojectrodrigoaugusto.service.NotificationReceiver
import com.example.finalprojectrodrigoaugusto.util.TimePicker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class TaskActivity : AppCompatActivity() {

    private var _binding: ActivityTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks")

    private var taskId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        loadTask()
        setUpTimeAndDate()
        setSaveTaskButtonListener()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Notification Channel INFNET"
        val descriptionText = "Channel for INFNET notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("INFNET", name, importance).apply {
            description = descriptionText
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNotification(data: String, hora: String) {
        val intent = Intent(this, NotificationReceiver::class.java)
        val title = "Título da notificação"
        val message = "Mensagem da notificação"

        intent.putExtra("title", title)
        intent.putExtra("message", message)


        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        val dataDia = data.substring(0, 2).toInt()
        val dataMes = data.substring(3, 5).toInt() - 1
        val dataAno = data.substring(6, 10).toInt()
        val horaHora = hora.substring(0, 2).toInt()
        val horaMinuto = hora.substring(3, 5).toInt()

        calendar.set(
            dataAno,
            dataMes,
            dataDia,
            horaHora,
            horaMinuto,
            0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
        Toast.makeText(this, "Notificação agendada", Toast.LENGTH_SHORT).show()
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
            it.hideKeyboard()

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
            it.hideKeyboard()

            handleTimePickerVisibility()

            TimePicker(this, binding.timePicker.timepicker).showTimePicker(
                binding.tasksEditTxtHour,
                binding.timePicker.timepickerBtSave,
                binding.timePicker.timepickerBtCancel,
                binding.timePicker.timepickerLayout

            )
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun createUpdateTask() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, uid)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "create_task")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        if (taskId !== "") {
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks/$taskId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    val task = snapshot.value as HashMap<String, String>

                    task["titulo"] = binding.tasksEdiTxtTitle.text.toString()
                    task["descricao"] = binding.tasksEdiTxtDescription.text.toString()
                    task["data"] = binding.tasksEditTxtDate.text.toString()
                    task["hora"] = binding.tasksEditTxtHour.text.toString()

                    ref.setValue(task)
                    Toast.makeText(
                        this@TaskActivity,
                        getString(R.string.tarefa_atualizada),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@TaskActivity,
                        getString(R.string.erro_ao_atualizar_tarefa),
                        Toast.LENGTH_SHORT
                    ).show()
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
        scheduleNotification(binding.tasksEditTxtDate.text.toString(), binding.tasksEditTxtHour.text.toString())
    }

    private fun loadTask() {
        this.taskId = intent.getStringExtra("id") ?: ""
        if (taskId === "") return

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks/$taskId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                binding.tasksEdiTxtTitle.setText(snapshot.child("titulo").value.toString())
                binding.tasksEdiTxtDescription.setText(snapshot.child("descricao").value.toString())
                binding.tasksEditTxtDate.setText(snapshot.child("data").value.toString())
                binding.tasksEditTxtHour.setText(snapshot.child("hora").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@TaskActivity,
                    getString(R.string.erro_ao_carregar_tarefa),
                    Toast.LENGTH_SHORT
                ).show()
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