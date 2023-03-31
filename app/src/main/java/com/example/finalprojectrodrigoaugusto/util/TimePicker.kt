package com.example.finalprojectrodrigoaugusto.util

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*

class TimePicker(var context: Context, private var timePicker: TimePicker) {

    private var hour = 0
    private var minute = 0

    fun showTimePicker(
        editText: EditText,
        saveButton: Button,
        cancelButton: Button,
        constraintLayout: ConstraintLayout
    ) {

        timePicker.setIs24HourView(true)

        timePicker.setOnTimeChangedListener { _, selectedHour, selectedMinute ->
            hour =  selectedHour
            minute = selectedMinute

            saveButton.setOnClickListener {
                editText.setText(String.format(Locale.getDefault(),"%02d:%02d", hour, minute))
                constraintLayout.visibility = View.GONE
            }
        }

        saveButton.setOnClickListener {
            editText.setText(String.format(Locale.getDefault(),"%02d:%02d",timePicker.hour, timePicker.minute))
            constraintLayout.visibility = View.GONE
        }

        cancelButton.setOnClickListener {
            constraintLayout.visibility = View.GONE
        }
    }
}