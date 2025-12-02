package com.example.aplicacion_cita_odontologica

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class pacientes_doctor : AppCompatActivity() {

    private lateinit var etBuscarPaciente: EditText
    private lateinit var containerPacientes: LinearLayout

    // Datos mock de pacientes
    private val pacientes = listOf(
        Paciente("Ana Martínez", "Próxima cita: 25/10/2023", "anamaria@email.com", "+34 600 123 456"),
        Paciente("Carlos Rodríguez", "Última visita: 15/08/2023", "carlos@email.com", "+34 611 234 567"),
        Paciente("Laura Gómez", "Próxima cita: 28/10/2023", "laura@email.com", "+34 622 345 678"),
        Paciente("Miguel Hernández", "Tratamiento en curso", "miguel@email.com", "+34 633 456 789"),
        Paciente("Alejandro Vargas", "Sin citas próximas", "alejandro@email.com", "+34 644 567 890"),
        Paciente("Beatriz Navarro", "Próxima cita: 30/10/2023", "beatriz@email.com", "+34 655 678 901"),
        Paciente("Carlos Jiménez", "Revisión post-endodoncia", "carlosj@email.com", "+34 666 789 012")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pacientes_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        cargarPacientes(pacientes)
        setupBusqueda()
    }

    private fun initViews() {
        etBuscarPaciente = findViewById(R.id.etBuscarPaciente)
        containerPacientes = findViewById(R.id.containerPacientes)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupBusqueda() {
        etBuscarPaciente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    cargarPacientes(pacientes)
                } else {
                    val resultados = pacientes.filter { paciente ->
                        paciente.nombre.lowercase().contains(query) ||
                                paciente.email.lowercase().contains(query)
                    }
                    cargarPacientes(resultados)
                }
            }
        })
    }

    private fun cargarPacientes(listaPacientes: List<Paciente>) {
        containerPacientes.removeAllViews()

        if (listaPacientes.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No se encontraron pacientes"
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
            containerPacientes.addView(tvEmpty)
            return
        }

        for (paciente in listaPacientes) {
            val cardView = com.google.android.material.card.MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                radius = 16f
                cardElevation = 4f
                setOnClickListener { verDetallePaciente(paciente) }
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(20, 20, 20, 20)
            }

            // Avatar
            val avatar = TextView(this).apply {
                text = paciente.nombre.substring(0, 1)
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    60,
                    60
                ).apply {
                    marginEnd = 16
                }
                background = getDrawable(R.drawable.circle_blue)
            }

            // Información
            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                }
            }

            val tvNombre = TextView(this).apply {
                text = paciente.nombre
                textSize = 18f
                setTextColor(Color.parseColor("#1A1A1A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 4
                }
            }

            val tvInfo = TextView(this).apply {
                text = paciente.info
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 4
                }
            }

            val tvEmail = TextView(this).apply {
                text = paciente.email
                textSize = 12f
                setTextColor(Color.parseColor("#999999"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            infoLayout.addView(tvNombre)
            infoLayout.addView(tvInfo)
            infoLayout.addView(tvEmail)

            // Flecha
            val flecha = TextView(this).apply {
                text = "➔"
                textSize = 20f
                setTextColor(Color.parseColor("#999999"))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    40,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            layout.addView(avatar)
            layout.addView(infoLayout)
            layout.addView(flecha)

            cardView.addView(layout)
            containerPacientes.addView(cardView)
        }
    }

    private fun verDetallePaciente(paciente: Paciente) {
        // Simulación de ver detalle
        android.widget.Toast.makeText(this,
            "Ver detalle de ${paciente.nombre}",
            android.widget.Toast.LENGTH_SHORT).show()
    }

    // Clase de datos para pacientes
    data class Paciente(
        val nombre: String,
        val info: String,
        val email: String,
        val telefono: String
    )
}