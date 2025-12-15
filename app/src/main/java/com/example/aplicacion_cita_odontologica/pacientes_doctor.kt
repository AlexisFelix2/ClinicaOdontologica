package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName

class pacientes_doctor : AppCompatActivity() {

    private lateinit var etBuscarPaciente: EditText
    private lateinit var containerPacientes: LinearLayout
    private lateinit var db: FirebaseFirestore
    private var listaCompletaPacientes = listOf<Paciente>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pacientes_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        initViews()

        val doctorId = intent.getStringExtra("doctor_id") ?: ""
        if (doctorId.isNotEmpty()) {
            cargarPacientesDesdeFirestore(doctorId)
        } else {
            Toast.makeText(this, "ID de doctor no proporcionado", Toast.LENGTH_SHORT).show()
        }
        setupBusqueda()
    }

    private fun initViews() {
        etBuscarPaciente = findViewById(R.id.etBuscarPaciente)
        containerPacientes = findViewById(R.id.containerPacientes)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }
    }

    private fun cargarPacientesDesdeFirestore(doctorId: String) {
        // 1. Encontrar los DNIs de los pacientes que tienen cita con el doctor
        db.collection("agendar_cita")
            .whereEqualTo("profesional", doctorId)
            .get()
            .addOnSuccessListener { citasSnapshot ->
                if (citasSnapshot.isEmpty) {
                    Log.d("FirestoreDebug", "No se encontraron citas para el doctor: $doctorId")
                    cargarPacientesEnUI(emptyList())
                    return@addOnSuccessListener
                }

                val dnis = citasSnapshot.documents.mapNotNull { it.getString("dni_cliente") }.distinct()

                if (dnis.isEmpty()) {
                    Log.d("FirestoreDebug", "Las citas encontradas no tienen DNI de cliente.")
                    cargarPacientesEnUI(emptyList())
                    return@addOnSuccessListener
                }

                Log.d("FirestoreDebug", "DNIs de pacientes encontrados: $dnis")

                // 2. Buscar los datos de esos pacientes en la colección 'cliente' usando el ID del documento
                db.collection("cliente")
                    .whereIn(FieldPath.documentId(), dnis)
                    .get()
                    .addOnSuccessListener { pacientesSnapshot ->
                        if (pacientesSnapshot.isEmpty) {
                            Log.d("FirestoreDebug", "No se encontraron perfiles para los DNIs: $dnis")
                        }
                        val pacientes = pacientesSnapshot.documents.mapNotNull { document ->
                            try {
                                val paciente = document.toObject(Paciente::class.java)
                                // Asignar el ID del documento (que es el DNI) al campo dni_cliente del objeto
                                paciente?.dni_cliente = document.id
                                paciente
                            } catch (e: Exception) {
                                Log.e("FirestoreError", "Error al convertir el documento de paciente ${document.id}", e)
                                null
                            }
                        }
                        Log.d("FirestoreDebug", "Pacientes procesados: ${pacientes.size}")
                        listaCompletaPacientes = pacientes
                        cargarPacientesEnUI(pacientes)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Error al buscar pacientes por ID: ", e)
                        Toast.makeText(this, "Error al cargar la lista de pacientes", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al buscar citas del doctor: ", e)
                Toast.makeText(this, "Error al cargar las citas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBusqueda() {
        etBuscarPaciente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val resultados = if (query.isEmpty()) {
                    listaCompletaPacientes
                } else {
                    listaCompletaPacientes.filter { paciente ->
                        paciente.nom_c.lowercase().contains(query) ||
                                paciente.ape_c.lowercase().contains(query) ||
                                paciente.dni_cliente.lowercase().contains(query)
                    }
                }
                cargarPacientesEnUI(resultados)
            }
        })
    }

    private fun cargarPacientesEnUI(listaPacientes: List<Paciente>) {
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
            val nombreCompleto = "${paciente.nom_c} ${paciente.ape_c}"

            // Avatar
            val avatar = TextView(this).apply {
                text = nombreCompleto.getOrNull(0)?.uppercase() ?: "?"
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(60, 60).apply { marginEnd = 16 }
                try {
                    background = getDrawable(R.drawable.circle_blue)
                } catch (e: Exception) {
                    setBackgroundColor(Color.BLUE) // Fallback color
                }
            }

            // Información
            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            }

            val tvNombre = TextView(this).apply {
                text = nombreCompleto
                textSize = 18f
                setTextColor(Color.parseColor("#1A1A1A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 4 }
            }

            val tvInfo = TextView(this).apply {
                text = "DNI: ${paciente.dni_cliente}"
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 4 }
            }

            val tvEmail = TextView(this).apply {
                text = paciente.correo_c
                textSize = 12f
                setTextColor(Color.parseColor("#999999"))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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
                layoutParams = LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.MATCH_PARENT)
            }

            layout.addView(avatar)
            layout.addView(infoLayout)
            layout.addView(flecha)

            cardView.addView(layout)
            containerPacientes.addView(cardView)
        }
    }

    private fun verDetallePaciente(paciente: Paciente) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)

            // Name
            addView(TextView(this@pacientes_doctor).apply {
                text = "${paciente.nom_c} ${paciente.ape_c}"
                textSize = 22f
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
            })

            // DNI
            addView(TextView(this@pacientes_doctor).apply {
                text = "DNI: ${paciente.dni_cliente}"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 40 }
            })

            // Separator
            val separator = View(this@pacientes_doctor).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                    bottomMargin = 40
                }
                setBackgroundColor(Color.LTGRAY)
            }
            addView(separator)

            // Email
            addView(TextView(this@pacientes_doctor).apply {
                text = "Email: ${paciente.correo_c}"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
            })

            // Phone
            addView(TextView(this@pacientes_doctor).apply {
                text = "Teléfono: ${paciente.telefono}"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
            })
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    data class Paciente(
        // No se usa @PropertyName porque se asigna manualmente
        var dni_cliente: String = "",
        @get:PropertyName("nom_c") @set:PropertyName("nom_c") var nom_c: String = "",
        @get:PropertyName("ape_c") @set:PropertyName("ape_c") var ape_c: String = "",
        @get:PropertyName("correo_c") @set:PropertyName("correo_c") var correo_c: String = "",
        @get:PropertyName("telefono") @set:PropertyName("telefono") var telefono: String = ""
    )
}
