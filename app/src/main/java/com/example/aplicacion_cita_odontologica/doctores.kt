package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class doctores : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var servicioSeleccionado: String
    private lateinit var contenedorDoctores: LinearLayout
    private lateinit var txtCargando: TextView
    private lateinit var txtNoDoctores: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccionar_doctor)

        // Verificar sesión de cliente
        verificarSesionCliente()

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        servicioSeleccionado = intent.getStringExtra("tipo_servicio") ?: "Servicio"

        // Inicializar vistas
        contenedorDoctores = findViewById(R.id.contenedorDoctores)
        txtCargando = findViewById(R.id.txtCargando)
        txtNoDoctores = findViewById(R.id.txtNoDoctores)

        // Cargar doctores desde Firebase
        cargarDoctoresDesdeFirebase()
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

    private fun cargarDoctoresDesdeFirebase() {
        db.collection("admin")
            .get()
            .addOnSuccessListener { documentos ->
                // Ocultar texto de carga
                txtCargando.visibility = View.GONE

                if (documentos.isEmpty) {
                    // Mostrar mensaje de no hay doctores
                    txtNoDoctores.visibility = View.VISIBLE
                    // Mostrar doctores de ejemplo para pruebas
                    mostrarDoctoresDeEjemplo()
                    Toast.makeText(this, "No hay doctores disponibles en la base de datos", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Limpiar contenedor
                contenedorDoctores.removeAllViews()

                for (documento in documentos) {
                    val idDoctor = documento.id
                    val nombre = documento.getString("nom_ad") ?: "Doctor"
                    val apellidos = documento.getString("ape_ad") ?: ""
                    val especialidad = documento.getString("especialidad") ?: "Odontólogo General"
                    val biografia = documento.getString("biografia") ?: ""

                    // Crear tarjeta dinámica para cada doctor
                    crearTarjetaDoctor(idDoctor, nombre, apellidos, especialidad, biografia)
                }
            }
            .addOnFailureListener { error ->
                txtCargando.visibility = View.GONE
                txtNoDoctores.visibility = View.VISIBLE
                // Mostrar doctores de ejemplo como respaldo
                mostrarDoctoresDeEjemplo()
                Toast.makeText(this, "Error al cargar doctores. Usando datos de ejemplo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDoctoresDeEjemplo() {
        // Mostrar los doctores de ejemplo que están en el XML
        val cardDoctorElena = findViewById<MaterialCardView>(R.id.cardDoctorElena)
        val cardDoctorCarlos = findViewById<MaterialCardView>(R.id.cardDoctorCarlos)

        cardDoctorElena.visibility = View.VISIBLE
        cardDoctorCarlos.visibility = View.VISIBLE
    }

    private fun crearTarjetaDoctor(
        idDoctor: String,
        nombre: String,
        apellidos: String,
        especialidad: String,
        biografia: String
    ) {
        // Determinar título (Dr. o Dra.)
        val titulo = if (nombre.startsWith("Dra.") || nombre.contains("Elena") || nombre.contains("Ana") || nombre.contains("María")) {
            "Dra."
        } else {
            "Dr."
        }

        val nombreCompleto = if (nombre.startsWith("Dr.") || nombre.startsWith("Dra.")) {
            "$nombre $apellidos"
        } else {
            "$titulo $nombre $apellidos"
        }

        // Crear MaterialCardView
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20.dpToPx()
            }
            radius = 16f.dpToPx().toFloat()
            elevation = 4f.dpToPx().toFloat()
            setOnClickListener {
                seleccionarDoctor(idDoctor, nombreCompleto, especialidad)
            }
        }

        val layoutInterno = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(
                20.dpToPx(),
                20.dpToPx(),
                20.dpToPx(),
                20.dpToPx()
            )
        }

        // Imagen de perfil
        val imageView = android.widget.ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                90.dpToPx(),
                90.dpToPx()
            )
            setImageResource(R.drawable.ic_profile)
            background = resources.getDrawable(R.drawable.circle_white)
            setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
        }

        val layoutInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginStart = 16.dpToPx()
            }
        }

        val txtNombre = TextView(this).apply {
            text = nombreCompleto
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val txtEspecialidad = TextView(this).apply {
            text = especialidad
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            setPadding(0, 4.dpToPx(), 0, 0)
        }

        val txtExperiencia = TextView(this).apply {
            text = "Especialista en $especialidad"
            textSize = 13f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            setPadding(0, 6.dpToPx(), 0, 0)
        }

        // Agregar elementos
        layoutInfo.addView(txtNombre)
        layoutInfo.addView(txtEspecialidad)
        layoutInfo.addView(txtExperiencia)

        layoutInterno.addView(imageView)
        layoutInterno.addView(layoutInfo)

        card.addView(layoutInterno)
        contenedorDoctores.addView(card)
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun Float.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun seleccionarDoctor(idDoctor: String, nombreDoctor: String, especialidad: String) {
        startActivity(
            Intent(this, seleccionar_fecha::class.java)
                .putExtra("tipo_servicio", servicioSeleccionado)
                .putExtra("id_profesional", idDoctor)  // Guardar ID del doctor (ej: EC001)
                .putExtra("nombre_profesional", nombreDoctor)
                .putExtra("especialidad_profesional", especialidad)
        )
    }

    fun volverALobby(view: View) {
        finish()
    }

    // Funciones originales para los doctores de ejemplo
    fun seleccionarDoctorElena(view: View) {
        startActivity(
            Intent(this, seleccionar_fecha::class.java)
                .putExtra("tipo_servicio", servicioSeleccionado)
                .putExtra("id_profesional", "EC001")
                .putExtra("nombre_profesional", "Dra. Elena Torres")
                .putExtra("especialidad_profesional", "Odontología General")
        )
    }

    fun seleccionarDoctorCarlos(view: View) {
        startActivity(
            Intent(this, seleccionar_fecha::class.java)
                .putExtra("tipo_servicio", servicioSeleccionado)
                .putExtra("id_profesional", "EC002")
                .putExtra("nombre_profesional", "Dr. Carlos Ramírez")
                .putExtra("especialidad_profesional", "Ortodoncia")
        )
    }
}