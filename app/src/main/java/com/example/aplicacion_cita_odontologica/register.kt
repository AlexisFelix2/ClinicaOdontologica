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

class register : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    // Declarar EditTexts
    private lateinit var inputNombre: EditText
    private lateinit var inputApellidos: EditText
    private lateinit var inputDNI: EditText
    private lateinit var inputTelefono: EditText
    private lateinit var inputCorreo: EditText
    private lateinit var inputPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        inicializarVistas()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inicializarVistas() {
        inputNombre = findViewById(R.id.inputNombre)
        inputApellidos = findViewById(R.id.inputApellidos)
        inputDNI = findViewById(R.id.inputDNI)
        inputTelefono = findViewById(R.id.inputTelefono)
        inputCorreo = findViewById(R.id.inputCorreo)
        inputPassword = findViewById(R.id.inputPassword)
    }

    // Función para el botón Atrás
    fun volverALogin(view: View) {
        val intent = Intent(this, login::class.java)
        startActivity(intent)
        finish() // Cerrar esta actividad
    }

    // Función para el botón Registrarse CON FIREBASE
    fun registrarUsuario(view: View) {
        // Obtener valores de los campos
        val nombre = inputNombre.text.toString().trim()
        val apellidos = inputApellidos.text.toString().trim()
        val dni = inputDNI.text.toString().trim().uppercase()
        val telefono = inputTelefono.text.toString().trim()
        val correo = inputCorreo.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        // Validar campos vacíos
        if (nombre.isEmpty()) {
            mostrarError(inputNombre, "Ingrese su nombre")
            return
        }
        if (apellidos.isEmpty()) {
            mostrarError(inputApellidos, "Ingrese sus apellidos")
            return
        }
        if (dni.isEmpty()) {
            mostrarError(inputDNI, "Ingrese su DNI")
            return
        }
        if (telefono.isEmpty()) {
            mostrarError(inputTelefono, "Ingrese su teléfono")
            return
        }
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

        // Validar contraseña (mínimo 6 caracteres)
        if (password.length < 6) {
            mostrarError(inputPassword, "Mínimo 6 caracteres")
            return
        }

        // Validar DNI (8 números, puede terminar con letra)
        val dniRegex = Regex("^[0-9]{8}[A-Z]?\$")
        if (!dniRegex.matches(dni)) {
            mostrarError(inputDNI, "DNI inválido (8 dígitos + letra)")
            return
        }

        // Mostrar que está procesando
        Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show()

        // Primero verificar si el DNI ya existe
        db.collection("cliente")
            .document(dni)
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    // El DNI ya está registrado
                    mostrarError(inputDNI, "DNI ya registrado")
                    Toast.makeText(this, "Este DNI ya está registrado", Toast.LENGTH_LONG).show()
                } else {
                    // Verificar si el correo ya existe
                    verificarCorreoUnico(nombre, apellidos, dni, telefono, correo, password)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun verificarCorreoUnico(
        nombre: String,
        apellidos: String,
        dni: String,
        telefono: String,
        correo: String,
        password: String
    ) {
        db.collection("cliente")
            .whereEqualTo("correo_c", correo)
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    // Correo único, proceder a registrar
                    guardarUsuario(nombre, apellidos, dni, telefono, correo, password)
                } else {
                    // Correo ya registrado
                    mostrarError(inputCorreo, "Correo ya registrado")
                    Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarUsuario(
        nombre: String,
        apellidos: String,
        dni: String,
        telefono: String,
        correo: String,
        password: String
    ) {
        // Crear mapa de datos según tu estructura
        val usuario = hashMapOf(
            "nom_c" to nombre,
            "ape_c" to apellidos,
            "correo_c" to correo,
            "password_c" to password,
            "telefono" to telefono
            // Nota: No incluimos "dni" aquí porque ya es el ID del documento
        )

        // Guardar en Firestore
        db.collection("cliente")
            .document(dni) // Usar DNI como ID del documento
            .set(usuario)
            .addOnSuccessListener {
                // Registro exitoso
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()

                // Guardar datos en SharedPreferences para mantener sesión
                val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                with(prefs.edit()) {
                    putString("dni", dni)
                    putString("nombre", nombre)
                    putString("apellidos", apellidos)
                    putString("correo", correo)
                    putBoolean("logueado", true)
                    apply()
                }

                // Ir al lobby
                val intent = Intent(this, login::class.java)
                intent.putExtra("dni", dni)
                intent.putExtra("nombre", "$nombre $apellidos")
                startActivity(intent)
                finish() // Cerrar esta actividad
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error al registrar: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarError(editText: EditText, mensaje: String) {
        editText.error = mensaje
        editText.requestFocus()
    }

    // Función para limpiar errores cuando el usuario empieza a escribir
    private fun limpiarErrores() {
        inputNombre.error = null
        inputApellidos.error = null
        inputDNI.error = null
        inputTelefono.error = null
        inputCorreo.error = null
        inputPassword.error = null
    }

    // Opcional: Puedes agregar TextWatchers para limpiar errores automáticamente
    private fun agregarTextWatchers() {
        // Ejemplo para el campo nombre
        inputNombre.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputNombre.error = null
        }
        inputApellidos.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputApellidos.error = null
        }
        inputDNI.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputDNI.error = null
        }
        inputTelefono.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputTelefono.error = null
        }
        inputCorreo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputCorreo.error = null
        }
        inputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputPassword.error = null
        }
    }

}