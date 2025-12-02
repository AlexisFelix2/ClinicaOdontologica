package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    // Función para el botón Iniciar Sesión
    fun iniciarSesion(view: View) {
        val intent = Intent(this, lobby::class.java)  // Va al lobby de PACIENTE
        startActivity(intent)
        finish()
    }

    // Función para el enlace Registrarme
    fun irARegistro(view: View) {
        val intent = Intent(this, register::class.java)
        startActivity(intent)
    }

    // ✅ NUEVA: Función para acceso rápido al doctor (SOLO PRUEBAS)
    fun accesoRapidoDoctor(view: View) {
        val intent = Intent(this, lobby_doctor::class.java)
        startActivity(intent)
        finish()
    }
}