package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class confirmacion : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var idCita: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmacion)

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener datos pasados
        idCita = intent.getStringExtra("id_cita") ?: ""
        val tipoServicio = intent.getStringExtra("tipo_servicio") ?: "Consulta Odontológica"
        val nombreProfesional = intent.getStringExtra("nombre_profesional") ?: "Doctor"
        val fecha = intent.getStringExtra("fecha") ?: ""
        val hora = intent.getStringExtra("hora") ?: ""
        val estado = intent.getStringExtra("estado") ?: "pendiente"

        // Mostrar datos en la interfaz
        findViewById<TextView>(R.id.txtServicio).text = tipoServicio
        findViewById<TextView>(R.id.txtProfesional).text = nombreProfesional
        findViewById<TextView>(R.id.txtFechaHora).text = "$fecha - $hora"
        findViewById<TextView>(R.id.txtEstado).text = estado.capitalizar()

        // Botón: Volver al inicio
        findViewById<Button>(R.id.btnVolverInicio).setOnClickListener {
            val intent = Intent(this, lobby::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Botón: Añadir al calendario
        findViewById<Button>(R.id.btnAgendarCalendario).setOnClickListener {
            if (fecha.isNotEmpty() && hora.isNotEmpty()) {
                agregarAlCalendario(tipoServicio, fecha, hora, nombreProfesional)
            } else {
                Toast.makeText(this, "No hay datos de fecha/hora para el calendario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun agregarAlCalendario(servicio: String, fecha: String, hora: String, doctor: String) {
        try {
            val partesFecha = fecha.split(" de ")
            if (partesFecha.size < 3) {
                Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show()
                return
            }

            val dia = partesFecha[0].toInt()
            val mes = obtenerMes(partesFecha[1])
            val anio = partesFecha[2].toInt()

            val partesHora = hora.split(":")
            val horaInt = partesHora[0].toInt()
            val minutoInt = partesHora[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(anio, mes, dia, horaInt, minutoInt)
            }

            val inicio = calendar.timeInMillis
            val fin = inicio + (30 * 60 * 1000) // 30 minutos

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin)
                putExtra(CalendarContract.Events.TITLE, "Cita Dental: $servicio")
                putExtra(CalendarContract.Events.DESCRIPTION, "Doctor: $doctor\nCita ID: $idCita")
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Clínica Dental")
            }

            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al agregar al calendario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

    private fun String.capitalizar(): String {
        return this.replaceFirstChar { it.uppercase() }
    }

    override fun onBackPressed() {
        // Evitar que vuelva atrás, debe usar el botón "Volver al inicio"
        val intent = Intent(this, lobby::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}