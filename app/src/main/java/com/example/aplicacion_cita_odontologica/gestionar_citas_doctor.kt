package com.example.aplicacion_cita_odontologica

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

class gestionar_citas_doctor : AppCompatActivity() {

    private lateinit var btnPendientes: Button
    private lateinit var btnConfirmadas: Button
    private lateinit var btnCanceladas: Button
    private lateinit var containerCitas: LinearLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestionar_citas_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        conectarFirebase()
        initViews()
        setupListeners()
        mostrarCitasPendientes()
    }

    private fun conectarFirebase() {
        db = FirebaseFirestore.getInstance()
    }

    private fun initViews() {
        try {
            btnPendientes = findViewById(R.id.btnPendientes)
            btnConfirmadas = findViewById(R.id.btnConfirmadas)
            btnCanceladas = findViewById(R.id.btnCanceladas)
            containerCitas = findViewById(R.id.containerCitas)

            findViewById<View>(R.id.btnBack).setOnClickListener {
                onBackPressed()
            }
        } catch (e: Exception) {
            Log.e("InitViewsError", "Error inicializando las vistas: ${e.message}", e)
            Toast.makeText(this, "Error al cargar la interfaz. Verifique que los IDs en el XML (ej. btnPendientes, containerCitas) son correctos.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        btnPendientes.setOnClickListener { mostrarCitasPendientes() }
        btnConfirmadas.setOnClickListener { mostrarCitasConfirmadas() }
        btnCanceladas.setOnClickListener { mostrarCitasCanceladas() }
    }

    private fun mostrarCitasPendientes() {
        actualizarBotones(btnPendientes, btnConfirmadas, btnCanceladas)
        cargarCitasPorEstado("pendiente")
    }

    private fun mostrarCitasConfirmadas() {
        actualizarBotones(btnConfirmadas, btnPendientes, btnCanceladas)
        cargarCitasPorEstado("confirmada")
    }

    private fun mostrarCitasCanceladas() {
        actualizarBotones(btnCanceladas, btnPendientes, btnConfirmadas)
        cargarCitasPorEstado("cancelada")
    }

    private fun cargarCitasPorEstado(estado: String) {
        db.collection("agendar_cita")
            .whereEqualTo("estado", estado)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("CitasError", "Error al cargar las citas", e)
                    Toast.makeText(this, "Error al cargar las citas", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val citas = snapshots?.mapNotNull { document ->
                    try {
                        val cita = document.toObject(CitaDoctor::class.java)
                        cita.copy(id = document.id)
                    } catch (ex: Exception) {
                        Log.e("CitaParseError", "Error al convertir el documento ${document.id}", ex)
                        null
                    }
                } ?: emptyList()

                cargarCitasEnUI(citas)
            }
    }


    private fun actualizarBotones(botonActivo: Button, boton1: Button, boton2: Button) {
        botonActivo.apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF6B35"))
        }

        boton1.apply {
            setTextColor(Color.parseColor("#666666"))
            setBackgroundColor(Color.WHITE)
        }

        boton2.apply {
            setTextColor(Color.parseColor("#666666"))
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun cargarCitasEnUI(citas: List<CitaDoctor>) {
        containerCitas.removeAllViews()

        if (citas.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No hay citas en esta categoría"
                textSize = 16f
                setTextColor(Color.parseColor("#666666"))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 100, 0, 0)
                }
            }
            containerCitas.addView(tvEmpty)
            return
        }

        for (cita in citas) {
            val cardView = com.google.android.material.card.MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                radius = 16f
                cardElevation = 4f
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(20, 20, 20, 20)
            }

            val tvNombre = TextView(this).apply {
                text = "DNI: ${cita.dni_cliente}"
                textSize = 18f
                setTextColor(Color.parseColor("#1A1A1A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            }

            val tvServicio = TextView(this).apply {
                text = cita.tipo_servicio
                textSize = 16f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            }

            val tvFecha = TextView(this).apply {
                text = if (cita.fecha_hora != null) {
                    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' hh:mm a", Locale("es", "ES"))
                    sdf.format(cita.fecha_hora!!.toDate())
                } else {
                    "Fecha no disponible"
                }
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            }

            val tvEstado = TextView(this).apply {
                text = cita.estado.uppercase()
                textSize = 12f
                when (cita.estado) {
                    "pendiente" -> setTextColor(Color.parseColor("#FF6B35"))
                    "confirmada" -> setTextColor(Color.parseColor("#4CAF50"))
                    "cancelada" -> setTextColor(Color.parseColor("#F44336"))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            }

            val layoutBotones = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            when (cita.estado) {
                "pendiente" -> {
                    val btnConfirmar = Button(this).apply {
                        text = "Confirmar"
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#4CAF50"))
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            weight = 1f
                            marginEnd = 8
                        }
                        setOnClickListener { confirmarCita(cita) }
                    }

                    val btnReprogramar = Button(this).apply {
                        text = "Reprogramar"
                        setTextColor(Color.parseColor("#666666"))
                        try {
                            background = getDrawable(R.drawable.rounded_border_gray)
                        } catch (e: Exception) {
                            Log.e("DrawableError", "No se encontró R.drawable.rounded_border_gray. Usando fondo por defecto.")
                        }
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            weight = 1f
                            marginEnd = 8
                        }
                    }

                    val btnCancelar = Button(this).apply {
                        text = "Cancelar"
                        setTextColor(Color.parseColor("#F44336"))
                        try {
                            background = getDrawable(R.drawable.rounded_border_red)
                        } catch (e: Exception) {
                            Log.e("DrawableError", "No se encontró R.drawable.rounded_border_red. Usando fondo por defecto.")
                        }
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            weight = 1f
                        }
                        setOnClickListener { cancelarCita(cita) }
                    }

                    layoutBotones.addView(btnConfirmar)
                    layoutBotones.addView(btnReprogramar)
                    layoutBotones.addView(btnCancelar)
                }
                "confirmada" -> { /* No buttons */ }
                "cancelada" -> { /* No buttons */ }
            }

            layout.addView(tvNombre)
            layout.addView(tvServicio)
            layout.addView(tvFecha)
            layout.addView(tvEstado)
            layout.addView(layoutBotones)

            cardView.addView(layout)
            containerCitas.addView(cardView)
        }
    }

    private fun confirmarCita(cita: CitaDoctor) {
        cita.id?.let {
            db.collection("agendar_cita").document(it)
                .update("estado", "confirmada")
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita confirmada", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cancelarCita(cita: CitaDoctor) {
        cita.id?.let {
            db.collection("agendar_cita").document(it)
                .update("estado", "cancelada")
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

@IgnoreExtraProperties
data class CitaDoctor(
    var id: String? = null,
    @get:PropertyName("dni_cliente")
    @set:PropertyName("dni_cliente")
    var dni_cliente: String = "",
    @get:PropertyName("tipo_servicio")
    @set:PropertyName("tipo_servicio")
    var tipo_servicio: String = "",
    @get:PropertyName("fecha_hora")
    @set:PropertyName("fecha_hora")
    var fecha_hora: Timestamp? = null,
    var estado: String = ""
)
