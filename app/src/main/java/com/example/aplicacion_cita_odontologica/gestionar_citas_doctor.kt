package com.example.aplicacion_cita_odontologica

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class gestionar_citas_doctor : AppCompatActivity() {

    private lateinit var btnPendientes: Button
    private lateinit var btnConfirmadas: Button
    private lateinit var btnCanceladas: Button
    private lateinit var containerCitas: LinearLayout

    // Datos mock
    private val citasPendientes = listOf(
        CitaDoctor("Carlos Santana", "Revisión General", "Hoy, 29 Nov - 10:30 AM", "pendiente"),
        CitaDoctor("María Lopez", "Limpieza Dental", "Jueves, 30 Nov - 04:00 PM", "pendiente"),
        CitaDoctor("Juan Rodríguez", "Consulta por Dolor", "Viernes, 1 Dic - 09:00 AM", "pendiente")
    )

    private val citasConfirmadas = listOf(
        CitaDoctor("Ana Martínez", "Ortodoncia Invisible", "25 Oct - 11:00 AM", "confirmada"),
        CitaDoctor("Carlos Rodríguez", "Blanqueamiento", "26 Oct - 03:30 PM", "confirmada"),
        CitaDoctor("Laura Gómez", "Revisión", "28 Oct - 10:00 AM", "confirmada")
    )

    private val citasCanceladas = listOf(
        CitaDoctor("Miguel Hernández", "Implante Dental", "20 Nov - 09:00 AM", "cancelada")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestionar_citas_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        mostrarCitasPendientes()
    }

    private fun initViews() {
        btnPendientes = findViewById(R.id.btnPendientes)
        btnConfirmadas = findViewById(R.id.btnConfirmadas)
        btnCanceladas = findViewById(R.id.btnCanceladas)
        containerCitas = findViewById(R.id.containerCitas)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        btnPendientes.setOnClickListener { mostrarCitasPendientes() }
        btnConfirmadas.setOnClickListener { mostrarCitasConfirmadas() }
        btnCanceladas.setOnClickListener { mostrarCitasCanceladas() }
    }

    private fun mostrarCitasPendientes() {
        actualizarBotones(btnPendientes, btnConfirmadas, btnCanceladas)
        cargarCitasEnUI(citasPendientes)
    }

    private fun mostrarCitasConfirmadas() {
        actualizarBotones(btnConfirmadas, btnPendientes, btnCanceladas)
        cargarCitasEnUI(citasConfirmadas)
    }

    private fun mostrarCitasCanceladas() {
        actualizarBotones(btnCanceladas, btnPendientes, btnConfirmadas)
        cargarCitasEnUI(citasCanceladas)
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

            // Nombre del paciente
            val tvNombre = TextView(this).apply {
                text = cita.nombrePaciente
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

            // Servicio
            val tvServicio = TextView(this).apply {
                text = cita.servicio
                textSize = 16f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            }

            // Fecha y hora
            val tvFecha = TextView(this).apply {
                text = cita.fechaHora
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            }

            // Estado
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

            // Botones de acción
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
                        background = getDrawable(R.drawable.rounded_border_gray)
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
                        background = getDrawable(R.drawable.rounded_border_red)
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
                "confirmada" -> {
                    val btnVerDetalles = Button(this).apply {
                        text = "Ver Detalles"
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#2196F3"))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    layoutBotones.addView(btnVerDetalles)
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
        // Simulación de confirmación
        android.widget.Toast.makeText(this, "Cita de ${cita.nombrePaciente} confirmada",
            android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun cancelarCita(cita: CitaDoctor) {
        // Simulación de cancelación
        android.widget.Toast.makeText(this, "Cita de ${cita.nombrePaciente} cancelada",
            android.widget.Toast.LENGTH_SHORT).show()
    }

    // Clase de datos para citas
    data class CitaDoctor(
        val nombrePaciente: String,
        val servicio: String,
        val fechaHora: String,
        val estado: String // pendiente, confirmada, cancelada
    )
}