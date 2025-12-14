package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View

class perfil_paciente : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_paciente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar sesión de cliente
        verificarSesionCliente()

        // Actualizar los TextView con datos del paciente logueado
        actualizarDatosPaciente()
    }

    private fun verificarSesionCliente() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "cliente") {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun actualizarDatosPaciente() {
        // Obtener datos del paciente desde SharedPreferences
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)

        val nombre = prefs.getString("nombre", "Paciente") ?: "Paciente"
        val apellidos = prefs.getString("apellidos", "") ?: ""
        val correo = prefs.getString("correo", "correo@ejemplo.com") ?: "correo@ejemplo.com"
        val dni = prefs.getString("dni", "00000000") ?: "00000000"
        val telefono = prefs.getString("telefono", "Sin teléfono") ?: "Sin teléfono"

        val nombreCompleto = "$nombre $apellidos".trim()

        // Tu XML NO tiene IDs para los TextView, así que vamos a reemplazar por el texto
        // 1. Reemplazar "Ana María López" (aparece 3 veces)
        reemplazarTextoEnTextView("Ana María López", nombreCompleto)

        // 2. Reemplazar "anamaria.lopez@email.com" (aparece 2 veces)
        reemplazarTextoEnTextView("anamaria.lopez@email.com", correo)

        // 3. Reemplazar "12345678A"
        reemplazarTextoEnTextView("12345678A", dni)

        // 4. Reemplazar "+34 600 123 456"
        reemplazarTextoEnTextView("+34 600 123 456", telefono)
    }

    private fun reemplazarTextoEnTextView(textoBuscar: String, textoNuevo: String) {
        // Busca en toda la vista un TextView que tenga el texto buscado y lo reemplaza
        val rootView = findViewById<android.view.View>(android.R.id.content)
        buscarYReemplazarTextView(rootView, textoBuscar, textoNuevo)
    }

    private fun buscarYReemplazarTextView(view: android.view.View, textoBuscar: String, textoNuevo: String) {
        if (view is android.widget.TextView) {
            // Si el texto del TextView es EXACTAMENTE igual al que buscamos, lo reemplazamos
            if (view.text.toString() == textoBuscar) {
                view.text = textoNuevo
                return
            }
        }

        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                buscarYReemplazarTextView(view.getChildAt(i), textoBuscar, textoNuevo)
            }
        }
    }
    fun volverALobby(view: View) {
        finish()
    }


}