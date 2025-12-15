package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class miscitas : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var contenedorCitas: LinearLayout
    private lateinit var txtCargando: TextView
    private lateinit var txtNoCitas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_miscitas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas - IMPORTANTE: necesitas agregar estos IDs al XML
        contenedorCitas = findViewById(R.id.contenedorCitas)
        txtCargando = findViewById(R.id.txtCargando)
        txtNoCitas = findViewById(R.id.txtNoCitas)

        // Verificar sesión y cargar citas
        verificarSesionCliente()
    }

    private fun verificarSesionCliente() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "cliente") {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        } else {
            // Si está logueado, cargar sus citas
            cargarMisCitas()
        }
    }

    private fun cargarMisCitas() {
        // Obtener DNI del cliente logueado
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val dniCliente = prefs.getString("dni", "") ?: ""

        if (dniCliente.isEmpty()) {
            Toast.makeText(this, "No se encontró DNI del cliente", Toast.LENGTH_SHORT).show()
            txtCargando.visibility = View.GONE
            txtNoCitas.visibility = View.VISIBLE
            return
        }

        // CONSULTA CON FILTRO: Solo las citas del cliente logueado
        db.collection("agendar_cita")
            .whereEqualTo("dni_cliente", dniCliente) // FILTRO POR DNI
            .get()
            .addOnSuccessListener { documentos ->
                txtCargando.visibility = View.GONE

                if (documentos.isEmpty) {
                    txtNoCitas.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                // Limpiar contenedor y eliminar citas de ejemplo
                contenedorCitas.removeAllViews()

                // Crear tarjeta para cada cita
                for (documento in documentos) {
                    val tipoServicio = documento.getString("tipo_servicio") ?: "Consulta"
                    val estado = documento.getString("estado") ?: "pendiente"
                    val fechaHora = documento.getTimestamp("fecha_hora")
                    val idProfesional = documento.getString("profesional") ?: ""

                    // Crear tarjeta de cita
                    crearTarjetaCita(tipoServicio, estado, fechaHora, idProfesional)
                }
            }
            .addOnFailureListener { error ->
                txtCargando.visibility = View.GONE
                txtNoCitas.visibility = View.VISIBLE
                Toast.makeText(this, "Error al cargar citas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearTarjetaCita(
        tipoServicio: String,
        estado: String,
        fechaHora: Timestamp?,
        idProfesional: String
    ) {
        // Formatear fecha y hora
        val fechaFormateada = if (fechaHora != null) {
            formatearFecha(fechaHora)
        } else {
            "Fecha no definida"
        }

        val horaFormateada = if (fechaHora != null) {
            formatearHora(fechaHora)
        } else {
            "Hora no definida"
        }

        // Crear MaterialCardView
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16.dpToPx()
                bottomMargin = 12.dpToPx()
            }
            radius = 12f.dpToPx().toFloat()
            elevation = 3f.dpToPx().toFloat()
        }

        val layoutInterno = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // Tipo de servicio
        val txtServicio = TextView(this).apply {
            text = tipoServicio
            textSize = 16f
            setTextColor(resources.getColor(R.color.primary_blue)) // #0096FF
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        // Estado
        val txtEstado = TextView(this).apply {
            text = estado.capitalizar()
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(
                when (estado.lowercase()) {
                    "confirmada" -> resources.getColor(android.R.color.holo_green_dark) // #4CAF50
                    "pendiente" -> resources.getColor(android.R.color.holo_orange_dark) // #FF9800
                    "cancelado" -> resources.getColor(android.R.color.holo_red_dark) // #F44336
                    "atendido" -> resources.getColor(android.R.color.holo_blue_dark) // #2196F3
                    else -> resources.getColor(android.R.color.darker_gray)
                }
            )
            setPadding(0, 4.dpToPx(), 0, 0)
        }

        // Mostrar ID del profesional temporalmente
        val txtProfesional = TextView(this).apply {
            text = "Profesional: $idProfesional"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            setPadding(0, 6.dpToPx(), 0, 0)
        }

        // Fecha
        val layoutFecha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10.dpToPx(), 0, 0)
        }

        val iconoFecha = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
            setImageResource(R.drawable.ic_calendar_check)
            setColorFilter(resources.getColor(android.R.color.darker_gray))
        }

        val txtFecha = TextView(this).apply {
            text = fechaFormateada
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8.dpToPx()
            }
        }

        layoutFecha.addView(iconoFecha)
        layoutFecha.addView(txtFecha)

        // Hora
        val layoutHora = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4.dpToPx(), 0, 0)
        }

        val iconoHora = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
            setImageResource(R.drawable.ic_time)
            setColorFilter(resources.getColor(android.R.color.darker_gray))
        }

        val txtHora = TextView(this).apply {
            text = horaFormateada
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8.dpToPx()
            }
        }

        layoutHora.addView(iconoHora)
        layoutHora.addView(txtHora)

        // Agregar elementos
        layoutInterno.addView(txtServicio)
        layoutInterno.addView(txtEstado)
        layoutInterno.addView(txtProfesional)
        layoutInterno.addView(layoutFecha)
        layoutInterno.addView(layoutHora)

        card.addView(layoutInterno)
        contenedorCitas.addView(card)
    }

    private fun formatearFecha(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        return sdf.format(date).replaceFirstChar { it.titlecase() }
    }

    private fun formatearHora(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val sdf = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
        return sdf.format(date)
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun Float.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun String.capitalizar(): String {
        return this.replaceFirstChar { it.uppercase() }
    }

    fun volverALobby(view: View) {
        finish()
    }
}