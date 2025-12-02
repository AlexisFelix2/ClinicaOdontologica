package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class servicios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_servicios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Función para el botón Atrás
    fun volverALobby(view: View) {
        val intent = Intent(this, lobby::class.java)
        startActivity(intent)
    }

    // Función para Limpieza Dental
    fun seleccionarLimpieza(view: View) {
        val intent = Intent(this, seleccionar_fecha::class.java)
        intent.putExtra("servicio", "Limpieza Dental Profesional")
        startActivity(intent)
    }

    // Función para Revisión General
    fun seleccionarRevision(view: View) {
        val intent = Intent(this, seleccionar_fecha::class.java)
        intent.putExtra("servicio", "Revisión General")
        startActivity(intent)
    }

    // Función para Ortodoncia
    fun seleccionarOrtodoncia(view: View) {
        val intent = Intent(this, seleccionar_fecha::class.java)
        intent.putExtra("servicio", "Ortodoncia")
        startActivity(intent)
    }

    // Función para Blanqueamiento
    fun seleccionarBlanqueamiento(view: View) {
        val intent = Intent(this, seleccionar_fecha::class.java)
        intent.putExtra("servicio", "Blanqueamiento Dental")
        startActivity(intent)
    }

    // Función para Implantes Dentales
    fun seleccionarImplantes(view: View) {
        val intent = Intent(this, seleccionar_fecha::class.java)
        intent.putExtra("servicio", "Implantes Dentales")
        startActivity(intent)
    }

    // Función para "No sé qué necesito"
    fun noSeQueNecesito(view: View) {
        // Aquí podría ir un asistente o información adicional
        // Por ahora redirigimos al lobby
        val intent = Intent(this, lobby::class.java)
        startActivity(intent)
    }
}