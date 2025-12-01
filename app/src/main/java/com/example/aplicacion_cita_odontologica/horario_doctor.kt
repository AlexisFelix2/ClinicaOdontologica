package com.example.aplicacion_cita_odontologica

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class horario_doctor : AppCompatActivity() {

    private lateinit var btnGuardarHorario: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_horario_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupSwitches()
        setupGuardarButton()
    }

    private fun initViews() {
        btnGuardarHorario = findViewById(R.id.btnGuardarHorario)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSwitches() {
        // Lunes
        val switchLunes: SwitchCompat = findViewById(R.id.switchLunes)
        val layoutHorarioLunes: LinearLayout = findViewById(R.id.layoutHorarioLunes)

        switchLunes.setOnCheckedChangeListener { _, isChecked ->
            layoutHorarioLunes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Martes
        val switchMartes: SwitchCompat = findViewById(R.id.switchMartes)
        val layoutHorarioMartes: LinearLayout = findViewById(R.id.layoutHorarioMartes)

        switchMartes.setOnCheckedChangeListener { _, isChecked ->
            layoutHorarioMartes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Miércoles
        val switchMiercoles: SwitchCompat = findViewById(R.id.switchMiercoles)
        val tvMiercolesNoDisponible: TextView = findViewById(R.id.tvMiercolesNoDisponible)

        switchMiercoles.setOnCheckedChangeListener { _, isChecked ->
            tvMiercolesNoDisponible.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Jueves
        val switchJueves: SwitchCompat = findViewById(R.id.switchJueves)
        val layoutHorarioJueves: LinearLayout = findViewById(R.id.layoutHorarioJueves)

        switchJueves.setOnCheckedChangeListener { _, isChecked ->
            layoutHorarioJueves.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Viernes
        val switchViernes: SwitchCompat = findViewById(R.id.switchViernes)
        val layoutHorarioViernes: LinearLayout = findViewById(R.id.layoutHorarioViernes)

        switchViernes.setOnCheckedChangeListener { _, isChecked ->
            layoutHorarioViernes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupGuardarButton() {
        btnGuardarHorario.setOnClickListener {
            guardarHorario()
        }
    }

    private fun guardarHorario() {
        // Recoger todos los datos del formulario
        val horario = HorarioDoctor(
            lunes = if (findViewById<SwitchCompat>(R.id.switchLunes).isChecked) {
                HorarioDia(
                    inicio = findViewById<EditText>(R.id.etLunesInicio).text.toString(),
                    fin = findViewById<EditText>(R.id.etLunesFin).text.toString()
                )
            } else null,

            martes = if (findViewById<SwitchCompat>(R.id.switchMartes).isChecked) {
                HorarioDia(
                    inicio = findViewById<EditText>(R.id.etMartesInicio).text.toString(),
                    fin = findViewById<EditText>(R.id.etMartesFin).text.toString()
                )
            } else null,

            miercoles = if (findViewById<SwitchCompat>(R.id.switchMiercoles).isChecked) {
                HorarioDia(
                    inicio = "09:00",
                    fin = "17:00"
                )
            } else null,

            jueves = if (findViewById<SwitchCompat>(R.id.switchJueves).isChecked) {
                HorarioDia(
                    inicio = findViewById<EditText>(R.id.etJuevesInicio).text.toString(),
                    fin = findViewById<EditText>(R.id.etJuevesFin).text.toString()
                )
            } else null,

            viernes = if (findViewById<SwitchCompat>(R.id.switchViernes).isChecked) {
                HorarioDia(
                    inicio = findViewById<EditText>(R.id.etViernesInicio).text.toString(),
                    fin = findViewById<EditText>(R.id.etViernesFin).text.toString()
                )
            } else null
        )

        // Mostrar mensaje de confirmación
        android.widget.Toast.makeText(this,
            "Horario guardado exitosamente",
            android.widget.Toast.LENGTH_SHORT).show()

        // Simulación de guardado
        // Aquí normalmente enviarías los datos al backend
        println("Horario guardado: $horario")
    }

    // Clases de datos para el horario
    data class HorarioDoctor(
        val lunes: HorarioDia?,
        val martes: HorarioDia?,
        val miercoles: HorarioDia?,
        val jueves: HorarioDia?,
        val viernes: HorarioDia?
    )

    data class HorarioDia(
        val inicio: String,
        val fin: String
    )
}