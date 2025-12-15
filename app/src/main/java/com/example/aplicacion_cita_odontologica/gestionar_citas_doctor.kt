package com.example.aplicacion_cita_odontologica

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private var doctorId: String? = null // To store the doctor's ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestionar_citas_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the doctor's ID from the intent that started this activity
        doctorId = intent.getStringExtra("doctor_id")
        if (doctorId == null) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del doctor.", Toast.LENGTH_LONG).show()
            finish() // Close the activity if the ID is missing
            return
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
        if (doctorId == null) {
            Toast.makeText(this, "ID de doctor no disponible.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("agendar_cita")
            .whereEqualTo("profesional", doctorId) // Filter by the current doctor's ID
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
                val fechaHoraTimestamp = cita.fecha_hora
                text = if (fechaHoraTimestamp != null) {
                    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' hh:mm a", Locale("es", "ES"))
                    sdf.format(fechaHoraTimestamp.toDate())
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
                        setTextColor(Color.parseColor("#4CAF50")) // Green text

                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.WHITE)
                            setStroke(4, Color.parseColor("#4CAF50"))
                        }
                        background = drawable

                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        ).apply {
                            weight = 1f
                            marginEnd = 8
                        }
                        setOnClickListener { confirmarCita(cita) }
                    }

                    val btnReprogramar = Button(this).apply {
                        text = "Reprogramar"
                        textSize = 9f
                        setTextColor(Color.parseColor("#666666"))
                        try {
                            background = getDrawable(R.drawable.rounded_border_gray)
                        } catch (e: Exception) {
                            Log.e("DrawableError", "No se encontró R.drawable.rounded_border_gray. Usando fondo por defecto.")
                        }
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        ).apply {
                            weight = 1f
                            marginEnd = 8
                        }
                        setOnClickListener { reprogramarCita(cita) }
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
                            LinearLayout.LayoutParams.MATCH_PARENT
                        ).apply {
                            weight = 1f
                        }
                        setOnClickListener { cancelarCita(cita) }
                    }

                    layoutBotones.addView(btnConfirmar)
                    layoutBotones.addView(btnReprogramar)
                    layoutBotones.addView(btnCancelar)
                }
                "confirmada" -> {
                    val btnVerDetalles = Button(this).apply {
                        text = "Ver Detalles"
                        setTextColor(Color.parseColor("#2196F3")) // Blue text
                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.WHITE)
                            setStroke(4, Color.parseColor("#2196F3"))
                        }
                        background = drawable
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setOnClickListener { mostrarDetallesCita(cita) }
                    }
                    layoutBotones.addView(btnVerDetalles)
                }
                "cancelada" -> {
                    val btnMotivo = Button(this).apply {
                        text = "Motivo de Cancelación"
                        setTextColor(Color.parseColor("#808080")) // Gray text
                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.WHITE)
                            setStroke(4, Color.parseColor("#808080"))
                        }
                        background = drawable
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setOnClickListener { mostrarDetallesCita(cita) }
                    }
                    layoutBotones.addView(btnMotivo)
                }
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
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Cancelar Cita")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (19 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val motivoInput = EditText(context).apply {
            hint = "Motivo de cancelación"
        }
        layout.addView(motivoInput)

        builder.setView(layout)

        builder.setPositiveButton("Confirmar") { _, _ ->
            val motivo = motivoInput.text.toString()
            if (motivo.isBlank()) {
                Toast.makeText(context, "Debe ingresar un motivo.", Toast.LENGTH_SHORT).show()
            } else {
                cita.id?.let {
                    val updates = mapOf(
                        "estado" to "cancelada",
                        "descripcion" to motivo
                    )
                    db.collection("agendar_cita").document(it)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Cita cancelada correctamente.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al cancelar la cita.", Toast.LENGTH_SHORT).show()
                            Log.e("CancelarCitaError", "Error al actualizar Firestore", e)
                        }
                }
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    private fun reprogramarCita(cita: CitaDoctor) {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Reprogramar Cita")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (19 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentDate = cita.fecha_hora?.toDate()

        val dateInput = EditText(context).apply {
            hint = "Nueva fecha (dd/MM/yyyy)"
            if (currentDate != null) {
                setText(dateFormat.format(currentDate))
            }
        }
        layout.addView(dateInput)

        val timeInput = EditText(context).apply {
            hint = "Nueva hora (HH:mm)"
            if (currentDate != null) {
                setText(timeFormat.format(currentDate))
            }
        }
        layout.addView(timeInput)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevaFechaStr = dateInput.text.toString()
            val nuevaHoraStr = timeInput.text.toString()

            if (nuevaFechaStr.isNotBlank() && nuevaHoraStr.isNotBlank()) {
                val dateTimeStr = "$nuevaFechaStr $nuevaHoraStr"
                val combinedFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                try {
                    val newDate = combinedFormat.parse(dateTimeStr)
                    if (newDate != null) {
                        val newTimestamp = Timestamp(newDate)
                        cita.id?.let {
                            db.collection("agendar_cita").document(it)
                                .update("fecha_hora", newTimestamp)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Cita reprogramada con éxito.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error al reprogramar la cita.", Toast.LENGTH_SHORT).show()
                                    Log.e("ReprogramarError", "Error al actualizar Firestore", e)
                                }
                        }
                    }
                } catch (e: java.text.ParseException) {
                    Toast.makeText(context, "Formato de fecha u hora inválido. Use dd/MM/yyyy y HH:mm.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Los campos de fecha y hora no pueden estar vacíos.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)

        builder.create().show()
    }

    private fun mostrarDetallesCita(cita: CitaDoctor) {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Detalles de la Cita")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (19 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' hh:mm a", Locale("es", "ES"))
        val fechaFormateada = cita.fecha_hora?.toDate()?.let { sdf.format(it) } ?: "No disponible"

        layout.addView(TextView(context).apply {
            text = "Paciente (DNI): ${cita.dni_cliente}"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        })
        layout.addView(TextView(context).apply {
            text = "Servicio: ${cita.tipo_servicio}"
            textSize = 16f
            setPadding(0, 0, 0, 8)
        })
        layout.addView(TextView(context).apply {
            text = "Descripción: ${cita.descripcion}"
            textSize = 16f
            setPadding(0, 0, 0, 8)
        })
        layout.addView(TextView(context).apply {
            text = "Fecha y Hora: $fechaFormateada"
            textSize = 16f
            setPadding(0, 0, 0, 8)
        })
        layout.addView(TextView(context).apply {
            text = "Estado: ${cita.estado.uppercase()}"
            textSize = 16f
            when (cita.estado) {
                "confirmada" -> setTextColor(Color.parseColor("#4CAF50"))
                "cancelada" -> setTextColor(Color.parseColor("#F44336"))
                else -> setTextColor(Color.GRAY)
            }
        })

        builder.setView(layout)
        builder.setPositiveButton("Cerrar", null)
        builder.create().show()
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
    var estado: String = "",
    var descripcion: String = ""
)
