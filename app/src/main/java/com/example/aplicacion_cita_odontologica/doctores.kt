package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class doctores : AppCompatActivity() {
    private lateinit var servicioSeleccionado: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccionar_doctor)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        servicioSeleccionado = intent.getStringExtra("servicio") ?: "Servicio"

    }


    fun volverALobby(view: View) {
        finish()
    }

    fun seleccionarDoctorElena(view: View) {
        startActivity(
            Intent(this, seleccionar_fecha::class.java)
                .putExtra("servicio", servicioSeleccionado)
                .putExtra("doctor", "Dra. Elena Torres")
                .putExtra("especialidad", "Odontología General")
        )
    }

    fun seleccionarDoctorCarlos(view: View) {
        startActivity(
            Intent(this, seleccionar_fecha::class.java)
                .putExtra("servicio", servicioSeleccionado)
                .putExtra("doctor", "Dr. Carlos Ramírez")
                .putExtra("especialidad", "Ortodoncia")
        )
    }
}
