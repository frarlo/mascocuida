package com.paco.mascocuida.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

// Basado en https://cursokotlin.com/capitulo-27-timepicker-en-kotlin/
class TimePickerFragment(val listener:(String) -> Unit): DialogFragment(), TimePickerDialog.OnTimeSetListener{

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val formattedTime = String.format("%02d:%02d",hourOfDay,minute)
        listener(formattedTime)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(activity as Context, this, hour, minute, true)
    }

}