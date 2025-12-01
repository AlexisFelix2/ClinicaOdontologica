package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        // Obtener datos del intent
        val servicio = intent.getStringExtra("servicio") ?: "Limpieza Dental"
        val fecha = intent.getStringExtra("fecha") ?: "29 de Octubre"
        val hora = intent.getStringExtra("hora") ?: "10:30 AM"

        // Configurar los textos en el layout
        findViewById<TextView>(R.id.txtServicio).text = servicio
        findViewById<TextView>(R.id.txtFechaHora).text = "$fecha - $hora"

        // Configurar botones
        findViewById<Button>(R.id.btnVolverInicio).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnAgendarCalendario).setOnClickListener {
            // Aquí iría la lógica para agregar al calendario
            // Por ahora, solo un mensaje o puedes omitir esta funcionalidad
        }
    }
}