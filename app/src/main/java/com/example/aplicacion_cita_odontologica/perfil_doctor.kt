package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class perfil_doctor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        cargarDatosDoctor()
    }

    private fun initViews() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.btnEditarPerfil).setOnClickListener {
            editarPerfil()
        }
    }

    private fun cargarDatosDoctor() {
        // Simulamos datos del doctor
        val datosDoctor = DatosDoctor(
            nombre = "Ana",
            apellidos = "García López",
            email = "ana.garcia@dental.com",
            especialidad = "Endodoncista",
            biografia = "Especialista en endodoncia con más de 9 años de experiencia en tratamientos de conducto complejos y microcirugía apical.",
            numeroColegiado = "12345"
        )

        findViewById<TextView>(R.id.tvNombreDoctor).text = datosDoctor.nombre
        findViewById<TextView>(R.id.tvApellidosDoctor).text = datosDoctor.apellidos
        findViewById<TextView>(R.id.tvEmailDoctor).text = datosDoctor.email
        findViewById<TextView>(R.id.tvEspecialidadDoctor).text = datosDoctor.especialidad
        findViewById<TextView>(R.id.tvBiografiaDoctor).text = datosDoctor.biografia
    }

    private fun editarPerfil() {
        // Simulación de edición
        android.widget.Toast.makeText(this,
            "Funcionalidad de edición (simulada)",
            android.widget.Toast.LENGTH_SHORT).show()
    }

    fun irAHorario(view: View) {
        val intent = Intent(this, horario_doctor::class.java)
        startActivity(intent)
    }

    // Clase de datos del doctor
    data class DatosDoctor(
        val nombre: String,
        val apellidos: String,
        val email: String,
        val especialidad: String,
        val biografia: String,
        val numeroColegiado: String
    )
}