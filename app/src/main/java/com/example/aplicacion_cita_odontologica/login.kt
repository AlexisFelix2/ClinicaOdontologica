package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class login : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var inputCorreo: EditText
    private lateinit var inputPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        inputCorreo = findViewById(R.id.inputCorreo)
        inputPassword = findViewById(R.id.inputPassword)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Función principal para iniciar sesión
    fun iniciarSesion(view: View) {
        // Obtener valores de los campos
        val correo = inputCorreo.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        // Validar campos vacíos
        if (correo.isEmpty()) {
            mostrarError(inputCorreo, "Ingrese su correo")
            return
        }
        if (password.isEmpty()) {
            mostrarError(inputPassword, "Ingrese su contraseña")
            return
        }

        // Validar formato de correo
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError(inputCorreo, "Correo inválido")
            return
        }

        // Mostrar que está procesando
        Toast.makeText(this, "Verificando credenciales...", Toast.LENGTH_SHORT).show()

        // PRIMERO: Intentar login como ADMIN/DOCTOR
        buscarAdmin(correo, password)
    }

    private fun buscarAdmin(correo: String, password: String) {
        db.collection("admin")
            .whereEqualTo("correo_ad", correo)
            .whereEqualTo("password_ad", password)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    // ADMIN encontrado
                    val documento = documentos.documents[0]
                    val adminId = documento.id
                    val nombre = documento.getString("nom_ad") ?: ""
                    val apellidos = documento.getString("ape_ad") ?: ""
                    val correo = documento.getString("correo_ad") ?: ""
                    val especialidad = documento.getString("especialidad") ?: "Odontólogo General"
                    val biografia = documento.getString("biografia") ?: ""

                    // Guardar sesión de admin en SharedPreferences con TODOS los datos
                    val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                    with(prefs.edit()) {
                        putString("tipo_usuario", "doctor")
                        putString("admin_id", adminId)
                        putString("nombre", nombre)
                        putString("apellidos", apellidos)
                        putString("correo", correo)
                        putString("especialidad", especialidad)
                        putString("biografia", biografia)
                        putBoolean("logueado", true)
                        apply()
                    }

                    // Mostrar mensaje de éxito
                    Toast.makeText(this, "¡Bienvenido Dr./Dra. $nombre!", Toast.LENGTH_SHORT).show()

                    // Ir al lobby de doctor
                    val intent = Intent(this, lobby_doctor::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // No es admin, buscar como CLIENTE
                    buscarCliente(correo, password)
                }
            }
            .addOnFailureListener { error ->
                // Si hay error en búsqueda de admin, buscar cliente
                buscarCliente(correo, password)
            }
    }

    private fun buscarCliente(correo: String, password: String) {
        db.collection("cliente")
            .whereEqualTo("correo_c", correo)
            .whereEqualTo("password_c", password)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    // CLIENTE encontrado
                    val documento = documentos.documents[0]
                    val dni = documento.id // El DNI es el ID del documento
                    val nombre = documento.getString("nom_c") ?: ""
                    val apellidos = documento.getString("ape_c") ?: ""
                    val telefono = documento.getString("telefono") ?: ""

                    // Guardar sesión en SharedPreferences
                    val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                    with(prefs.edit()) {
                        putString("tipo_usuario", "cliente")
                        putString("dni", dni)
                        putString("nombre", nombre)
                        putString("apellidos", apellidos)
                        putString("correo", correo)
                        putString("telefono", telefono)
                        putBoolean("logueado", true)
                        apply()
                    }

                    // Mostrar mensaje de éxito
                    Toast.makeText(this, "¡Bienvenido $nombre!", Toast.LENGTH_SHORT).show()

                    // Ir al lobby de paciente
                    val intent = Intent(this, lobby::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Credenciales incorrectas
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
                    inputPassword.text.clear()
                    inputPassword.requestFocus()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Función para el enlace Registrarme
    fun irARegistro(view: View) {
        val intent = Intent(this, register::class.java)
        startActivity(intent)
    }

    // Función para acceso rápido al doctor (SOLO PRUEBAS - DESACTIVAR EN PRODUCCIÓN)
    fun accesoRapidoDoctor(view: View) {
        // Solo para pruebas - datos de doctor de prueba
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("tipo_usuario", "doctor")
            putString("admin_id", "EC001")
            putString("nombre", "María")
            putString("apellidos", "García")
            putString("correo", "doctor@clinica.com")
            putBoolean("logueado", true)
            apply()
        }

        Toast.makeText(this, "Modo prueba - Acceso como Doctor", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, lobby_doctor::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarError(editText: EditText, mensaje: String) {
        editText.error = mensaje
        editText.requestFocus()
    }

    // Función para el TextView "¿Olvidaste tu contraseña?"
    fun olvidarContrasena(view: View) {
        val intent = Intent(this, RecuperarPasswordActivity::class.java)
        startActivity(intent)
    }

}