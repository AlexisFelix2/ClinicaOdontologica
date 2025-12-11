package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
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

        // Verificar sesión de doctor
        verificarSesionDoctor()

        // Actualizar los TextView con datos del doctor logueado
        actualizarDatosDoctor()
    }

    private fun verificarSesionDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "doctor") {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun actualizarDatosDoctor() {
        // Obtener datos del doctor desde SharedPreferences
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)

        val nombre = prefs.getString("nombre", "Doctor") ?: "Doctor"
        val apellidos = prefs.getString("apellidos", "") ?: ""
        val correo = prefs.getString("correo", "doctor@clinica.com") ?: "doctor@clinica.com"
        val especialidad = prefs.getString("especialidad", "Odontólogo General") ?: "Odontólogo General"
        val biografia = prefs.getString("biografia", "Biografía no disponible") ?: "Biografía no disponible"

        // 1. Actualizar TextView que dice "Dra. Ana García" (el que está debajo de la foto)
        // Tu XML tiene este texto en un TextView sin ID, así que vamos a buscarlo
        val nombreCompletoDoctor = "Dr./Dra. $nombre $apellidos"

        // Reemplazar "Dra. Ana García" por el nombre real
        reemplazarTextoEnTextView("Dra. Ana García", nombreCompletoDoctor)

        // 2. Actualizar los TextView que SÍ tienen ID en tu XML
        findViewById<android.widget.TextView>(R.id.tvNombreDoctor).text = nombre
        findViewById<android.widget.TextView>(R.id.tvApellidosDoctor).text = apellidos
        findViewById<android.widget.TextView>(R.id.tvEmailDoctor).text = correo
        findViewById<android.widget.TextView>(R.id.tvEspecialidadDoctor).text = especialidad
        findViewById<android.widget.TextView>(R.id.tvBiografiaDoctor).text = biografia

        // 3. Actualizar "Número de Colegiado: 12345" (si quieres poner algo personalizado)
        reemplazarTextoEnTextView("Número de Colegiado: 12345", "Número de Colegiado: ODON-2024")
    }

    private fun reemplazarTextoEnTextView(textoBuscar: String, textoNuevo: String) {
        // Busca en toda la vista un TextView que tenga el texto buscado y lo reemplaza
        val rootView = findViewById<android.view.View>(android.R.id.content)
        buscarYReemplazarTextView(rootView, textoBuscar, textoNuevo)
    }

    private fun buscarYReemplazarTextView(view: android.view.View, textoBuscar: String, textoNuevo: String) {
        if (view is android.widget.TextView && view.text.toString() == textoBuscar) {
            view.text = textoNuevo
            return
        }

        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                buscarYReemplazarTextView(view.getChildAt(i), textoBuscar, textoNuevo)
            }
        }
    }

    // Botón de editar
    fun editarPerfil(view: android.view.View) {
        android.widget.Toast.makeText(this, "Funcionalidad de edición en desarrollo", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun irAHorario(view: android.view.View) {
        val intent = Intent(this, horario_doctor::class.java)
        startActivity(intent)
    }

    // Botón de retroceso
    fun volver(view: android.view.View) {
        onBackPressed()
    }
}