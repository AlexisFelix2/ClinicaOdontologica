package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class perfil_doctor : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    // Vistas de la UI
    private lateinit var tvNombreCompleto: TextView
    private lateinit var tvEspecialidad: TextView
    private lateinit var tvNombreDoctor: TextView
    private lateinit var tvApellidosDoctor: TextView
    private lateinit var tvEmailDoctor: TextView
    private lateinit var tvEspecialidadDoctor: TextView
    private lateinit var tvBiografiaDoctor: TextView

    // Vistas de Edición
    private lateinit var etNombreDoctor: EditText
    private lateinit var etApellidosDoctor: EditText
    private lateinit var etEspecialidadDoctor: EditText
    private lateinit var etBiografiaDoctor: EditText

    private lateinit var btnEditarPerfil: Button

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializaciones
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)

        verificarSesionDoctor()
        initViews()
        actualizarDatosDoctor()
    }

    private fun initViews() {
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto)
        tvEspecialidad = findViewById(R.id.tvEspecialidad)
        tvNombreDoctor = findViewById(R.id.tvNombreDoctor)
        tvApellidosDoctor = findViewById(R.id.tvApellidosDoctor)
        tvEmailDoctor = findViewById(R.id.tvEmailDoctor)
        tvEspecialidadDoctor = findViewById(R.id.tvEspecialidadDoctor)
        tvBiografiaDoctor = findViewById(R.id.tvBiografiaDoctor)

        // Asumimos que los EditText existen en el XML con visibility="gone"
        etNombreDoctor = findViewById(R.id.etNombreDoctor)
        etApellidosDoctor = findViewById(R.id.etApellidosDoctor)
        etEspecialidadDoctor = findViewById(R.id.etEspecialidadDoctor)
        etBiografiaDoctor = findViewById(R.id.etBiografiaDoctor)

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil)
    }

    private fun verificarSesionDoctor() {
        if (!prefs.getBoolean("logueado", false) || prefs.getString("tipo_usuario", "") != "doctor") {
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

    private fun actualizarDatosDoctor() {
        val nombre = prefs.getString("nombre", "") ?: ""
        val apellidos = prefs.getString("apellidos", "") ?: ""
        val correo = prefs.getString("correo", "") ?: ""
        val especialidad = prefs.getString("especialidad", "") ?: ""
        val biografia = prefs.getString("biografia", "") ?: ""

        tvNombreCompleto.text = "Dr./Dra. $nombre $apellidos"
        tvEspecialidad.text = especialidad
        tvNombreDoctor.text = nombre
        tvApellidosDoctor.text = apellidos
        tvEmailDoctor.text = correo
        tvEspecialidadDoctor.text = especialidad
        tvBiografiaDoctor.text = biografia
    }

    fun editarPerfil(view: View) {
        isEditMode = !isEditMode
        if (isEditMode) {
            enterEditMode()
        } else {
            saveProfileAndExitEditMode()
        }
    }

    private fun enterEditMode() {
        // Ocultar TextViews y mostrar EditTexts
        tvNombreDoctor.visibility = View.GONE
        etNombreDoctor.visibility = View.VISIBLE
        etNombreDoctor.setText(tvNombreDoctor.text)

        tvApellidosDoctor.visibility = View.GONE
        etApellidosDoctor.visibility = View.VISIBLE
        etApellidosDoctor.setText(tvApellidosDoctor.text)

        tvEspecialidadDoctor.visibility = View.GONE
        etEspecialidadDoctor.visibility = View.VISIBLE
        etEspecialidadDoctor.setText(tvEspecialidadDoctor.text)

        tvBiografiaDoctor.visibility = View.GONE
        etBiografiaDoctor.visibility = View.VISIBLE
        etBiografiaDoctor.setText(tvBiografiaDoctor.text)

        btnEditarPerfil.text = "Guardar"
    }

    private fun saveProfileAndExitEditMode() {
        val doctorId = prefs.getString("admin_id", null)
        if (doctorId == null) {
            Toast.makeText(this, "Error: ID de sesión no encontrado.", Toast.LENGTH_SHORT).show()
            exitEditMode() // Salir del modo edición para evitar problemas
            return
        }

        val nuevosDatos = mapOf(
            "nom_ad" to etNombreDoctor.text.toString(),
            "ape_ad" to etApellidosDoctor.text.toString(),
            "especialidad" to etEspecialidadDoctor.text.toString(),
            "biografia" to etBiografiaDoctor.text.toString()
        )

        db.collection("admin").document(doctorId).update(nuevosDatos)
            .addOnSuccessListener {
                // Actualizar SharedPreferences
                with(prefs.edit()) {
                    putString("nombre", nuevosDatos["nom_ad"])
                    putString("apellidos", nuevosDatos["ape_ad"])
                    putString("especialidad", nuevosDatos["especialidad"])
                    putString("biografia", nuevosDatos["biografia"])
                    apply()
                }
                Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                exitEditMode()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "Error al actualizar el perfil", e)
                Toast.makeText(this, "Error al guardar los cambios.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exitEditMode() {
        actualizarDatosDoctor() // Refrescar los datos desde SharedPreferences

        // Ocultar EditTexts y mostrar TextViews
        tvNombreDoctor.visibility = View.VISIBLE
        etNombreDoctor.visibility = View.GONE

        tvApellidosDoctor.visibility = View.VISIBLE
        etApellidosDoctor.visibility = View.GONE

        tvEspecialidadDoctor.visibility = View.VISIBLE
        etEspecialidadDoctor.visibility = View.GONE

        tvBiografiaDoctor.visibility = View.VISIBLE
        etBiografiaDoctor.visibility = View.GONE

        btnEditarPerfil.text = "Editar Perfil"
    }

    fun irAHorario(view: View) {
        startActivity(Intent(this, horario_doctor::class.java))
    }

    fun volver(view: View) {
        onBackPressed()
    }
}