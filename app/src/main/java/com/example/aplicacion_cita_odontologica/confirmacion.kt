package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class confirmacion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmacion)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val servicio = intent.getStringExtra("servicio") ?: "Consulta Odontológica"
        val fecha = intent.getStringExtra("fecha") ?: ""
        val hora = intent.getStringExtra("hora") ?: ""

        findViewById<TextView>(R.id.txtServicio).text = servicio
        findViewById<TextView>(R.id.txtFechaHora).text = "$fecha - $hora"

        // Volver al inicio
        findViewById<Button>(R.id.btnVolverInicio).setOnClickListener {
            val intent = Intent(this, lobby::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Añadir al calendario
        findViewById<Button>(R.id.btnAgendarCalendario).setOnClickListener {
            agregarAlCalendario(servicio, fecha, hora)
        }
    }

    private fun agregarAlCalendario(servicio: String, fecha: String, hora: String) {
        val calendar = Calendar.getInstance()

        val partesFecha = fecha.split(" de ")
        val dia = partesFecha[0].toInt()
        val mes = obtenerMes(partesFecha[1])
        val anio = partesFecha[2].toInt()

        val partesHora = hora.split(":")
        val horaInt = partesHora[0].toInt()
        val minutoInt = partesHora[1].toInt()

        calendar.set(anio, mes, dia, horaInt, minutoInt)

        val inicio = calendar.timeInMillis
        val fin = inicio + (30 * 60 * 1000) // 30 minutos

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin)
            putExtra(CalendarContract.Events.TITLE, servicio)
            putExtra(CalendarContract.Events.DESCRIPTION, "Cita odontológica")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Clínica Dental")
        }

        startActivity(intent)
    }

    private fun obtenerMes(mes: String): Int {
        return when (mes) {
            "Enero" -> 0
            "Febrero" -> 1
            "Marzo" -> 2
            "Abril" -> 3
            "Mayo" -> 4
            "Junio" -> 5
            "Julio" -> 6
            "Agosto" -> 7
            "Septiembre" -> 8
            "Octubre" -> 9
            "Noviembre" -> 10
            "Diciembre" -> 11
            else -> 0
        }
    }
}
