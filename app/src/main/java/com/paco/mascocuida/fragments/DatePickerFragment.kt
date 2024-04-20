package com.paco.mascocuida.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

/*
*  Esta clase es un fragment (basado en https://cursokotlin.com/capitulo-26-datepicker-en-kotlin/). Que nos
*  permite invocar un emergente para elegir una fecha especifica con un widget de tipo calendario.
*/
class DatePickerFragment(val listener: (day: Int, month: Int, year: Int) -> Unit):
DialogFragment(), DatePickerDialog.OnDateSetListener{

    // Función que recoge la fecha elegida en el Fragment:
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Enero es "0" Por lo que para mostrar correctamente el mes debemos añadir un +1:
        listener(dayOfMonth, month + 1, year)
    }

    // Función que crea el Fragment para elegir fecha:
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(activity as Context, this, year, month, day)
    }
}