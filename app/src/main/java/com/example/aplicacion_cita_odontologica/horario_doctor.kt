package com.example.aplicacion_cita_odontologica

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class horario_doctor : AppCompatActivity() {

    private lateinit var btnGuardarHorario: Button

    // Lista de Views por día para simplificar el manejo
    private val diaViews = listOf(
        DiaViewReferences(R.id.switchLunes, R.id.layoutHorarioLunes, R.id.etLunesInicio, R.id.etLunesFin, R.id.tvLunesNoDisponible),
        DiaViewReferences(R.id.switchMartes, R.id.layoutHorarioMartes, R.id.etMartesInicio, R.id.etMartesFin, R.id.tvMartesNoDisponible),
        // Miércoles ya tiene un EditText de inicio/fin en el XML, aunque por defecto está oculto
        DiaViewReferences(R.id.switchMiercoles, R.id.layoutHorarioMiercoles, R.id.etMiercolesInicio, R.id.etMiercolesFin, R.id.tvMiercolesNoDisponible),
        DiaViewReferences(R.id.switchJueves, R.id.layoutHorarioJueves, R.id.etJuevesInicio, R.id.etJuevesFin, R.id.tvJuevesNoDisponible),
        DiaViewReferences(R.id.switchViernes, R.id.layoutHorarioViernes, R.id.etViernesInicio, R.id.etViernesFin, R.id.tvViernesNoDisponible)
    )

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

        // Asignar IDs para Miércoles (se agregaron en el XML)
        findViewById<EditText>(R.id.etMiercolesInicio).apply {
            setOnClickListener { showTimePickerDialog(this) }
        }
        findViewById<EditText>(R.id.etMiercolesFin).apply {
            setOnClickListener { showTimePickerDialog(this) }
        }
    }

    private fun setupSwitches() {
        diaViews.forEach { setupDay(it) }
    }

    private fun setupDay(refs: DiaViewReferences) {
        val switch: SwitchCompat = findViewById(refs.switchId)
        val layoutHorario: LinearLayout = findViewById(refs.layoutHorarioId)
        val etInicio: EditText = findViewById(refs.etInicioId)
        val etFin: EditText = findViewById(refs.etFinId)
        val tvNoDisponible: TextView = findViewById(refs.tvNoDisponibleId)

        // Manejar el cambio de estado del switch
        switch.setOnCheckedChangeListener { _, isChecked ->
            layoutHorario.visibility = if (isChecked) View.VISIBLE else View.GONE
            tvNoDisponible.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Configurar TimePicker para los EditText
        etInicio.setOnClickListener { showTimePickerDialog(etInicio) }
        etFin.setOnClickListener { showTimePickerDialog(etFin) }

        // Inicializar la visibilidad basada en el estado inicial del switch (del XML)
        layoutHorario.visibility = if (switch.isChecked) View.VISIBLE else View.GONE
        tvNoDisponible.visibility = if (switch.isChecked) View.GONE else View.VISIBLE
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()

        // Intenta obtener la hora actual del EditText si no está vacío
        val initialTime = editText.text.toString()
        val (currentHour, currentMinute) = if (initialTime.contains(":")) {
            initialTime.split(":").map { it.toIntOrNull() ?: 0 }
        } else {
            listOf(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            editText.setText(formattedTime)
        }

        // Usamos TimePickerDialog con formato 24 horas (is24HourView = true)
        TimePickerDialog(this, timeSetListener, currentHour, currentMinute, true).show()
    }

    private fun setupGuardarButton() {
        btnGuardarHorario.setOnClickListener {
            guardarHorario()
        }
    }

    private fun guardarHorario() {
        // Función auxiliar para obtener el HorarioDia o null
        fun getHorarioDia(refs: DiaViewReferences): HorarioDia? {
            val switch: SwitchCompat = findViewById(refs.switchId)
            return if (switch.isChecked) {
                HorarioDia(
                    inicio = findViewById<EditText>(refs.etInicioId).text.toString(),
                    fin = findViewById<EditText>(refs.etFinId).text.toString()
                )
            } else null
        }

        val horario = HorarioDoctor(
            lunes = getHorarioDia(diaViews[0]),
            martes = getHorarioDia(diaViews[1]),
            miercoles = getHorarioDia(diaViews[2]),
            jueves = getHorarioDia(diaViews[3]),
            viernes = getHorarioDia(diaViews[4])
        )

        // Validación simple
        val diasIncompletos = horario.toList().filterNotNull().filter { it.inicio.isBlank() || it.fin.isBlank() }
        if (diasIncompletos.isNotEmpty()) {
            Toast.makeText(this, "Por favor, rellena las horas de todos los días marcados.", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this,
            "Horario guardado exitosamente",
            Toast.LENGTH_SHORT).show()

        // Simulación de guardado
        println("Horario guardado: $horario")
    }

    private data class DiaViewReferences(
        val switchId: Int,
        val layoutHorarioId: Int,
        val etInicioId: Int,
        val etFinId: Int,
        val tvNoDisponibleId: Int
    )

    data class HorarioDoctor(
        val lunes: HorarioDia?,
        val martes: HorarioDia?,
        val miercoles: HorarioDia?,
        val jueves: HorarioDia?,
        val viernes: HorarioDia?
    ) {
        fun toList() = listOf(lunes, martes, miercoles, jueves, viernes)
    }

    data class HorarioDia(
        val inicio: String,
        val fin: String
    )
}