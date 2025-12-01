package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*
import android.widget.GridView
class seleccionar_fecha : AppCompatActivity() {

    private var fechaSeleccionada: String = ""
    private var horaSeleccionada: String = ""
    private var calendar: Calendar = Calendar.getInstance()
    private lateinit var gridCalendario: GridView
    private lateinit var txtMesAnio: TextView
    private lateinit var gridHorarios: GridLayout
    private lateinit var txtHorariosTitle: TextView
    private lateinit var btnConfirmar: Button

    // Horarios disponibles por día (simulados)
    private val horariosDisponibles = mapOf(
        "29-10-2024" to listOf("09:00", "09:45", "11:15", "12:00", "12:45", "14:30", "15:15"),
        "30-10-2024" to listOf("10:00", "10:45", "11:30", "14:00", "15:00", "16:00"),
        "31-10-2024" to listOf("08:30", "09:15", "10:00", "13:00", "14:30", "15:45")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccionar_fecha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el servicio seleccionado
        val servicio = intent.getStringExtra("servicio") ?: "Limpieza Dental"
        findViewById<TextView>(R.id.txtServicio).text = servicio

        initViews()
        setupCalendar()
        setupNavigation()
        setupConfirmButton()
    }

    private fun initViews() {
        gridCalendario = findViewById(R.id.gridCalendario)
        txtMesAnio = findViewById(R.id.txtMesAnio)
        gridHorarios = findViewById(R.id.gridHorarios)
        txtHorariosTitle = findViewById(R.id.txtHorariosTitle)
        btnConfirmar = findViewById(R.id.btnConfirmarHorario)
    }

    private fun setupCalendar() {
        updateCalendar()

        gridCalendario.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            // Resetear selección anterior
            resetCalendarSelection()

            // Seleccionar nuevo día
            val dia = view as TextView
            if (dia.text.isNotEmpty() && dia.text != "") {
                dia.setBackgroundColor(Color.parseColor("#0099FF"))
                dia.setTextColor(Color.WHITE)

                val diaSeleccionado = dia.text.toString().toInt()
                val mes = calendar.get(Calendar.MONTH) + 1
                val anio = calendar.get(Calendar.YEAR)

                fechaSeleccionada = "$diaSeleccionado-$mes-$anio"
                mostrarHorariosDisponibles(fechaSeleccionada)
            }
        }
    }

    private fun resetCalendarSelection() {
        for (i in 0 until gridCalendario.childCount) {
            val child = gridCalendario.getChildAt(i) as TextView
            if (child.text.isNotEmpty()) {
                child.setBackgroundColor(Color.TRANSPARENT)
                child.setTextColor(Color.parseColor("#333333"))
            }
        }
    }

    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        txtMesAnio.text = dateFormat.format(calendar.time)

        val days = getDaysInMonth()
        val adapter = DayAdapter(this, days)
        gridCalendario.adapter = adapter
    }

    private fun getDaysInMonth(): List<String> {
        val days = mutableListOf<String>()
        val calendarCopy = calendar.clone() as Calendar
        calendarCopy.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendarCopy.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendarCopy.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Días vacíos al inicio
        for (i in 1 until firstDayOfWeek) {
            days.add("")
        }

        // Días del mes
        for (i in 1..daysInMonth) {
            days.add(i.toString())
        }

        return days
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.btnMesAnterior).setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
            resetHorarios()
        }

        findViewById<Button>(R.id.btnMesSiguiente).setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
            resetHorarios()
        }
    }

    private fun mostrarHorariosDisponibles(fecha: String) {
        gridHorarios.removeAllViews()
        val horarios = horariosDisponibles[fecha] ?: emptyList()

        if (horarios.isNotEmpty()) {
            txtHorariosTitle.text = "Horarios para el ${formatearFecha(fecha)}"
            gridHorarios.visibility = View.VISIBLE

            for (horario in horarios) {
                val button = Button(this).apply {
                    text = horario
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(8, 8, 8, 8)
                    }
                    setBackgroundColor(Color.parseColor("#E3F2FD"))
                    setTextColor(Color.parseColor("#0099FF"))
                    setOnClickListener {
                        seleccionarHorario(horario, this)
                    }
                }
                gridHorarios.addView(button)
            }
        } else {
            txtHorariosTitle.text = "No hay horarios disponibles para el ${formatearFecha(fecha)}"
            gridHorarios.visibility = View.GONE
        }
    }

    private fun seleccionarHorario(horario: String, button: Button) {
        horaSeleccionada = horario
        // Resetear selección de horarios
        for (i in 0 until gridHorarios.childCount) {
            val child = gridHorarios.getChildAt(i) as Button
            child.setBackgroundColor(Color.parseColor("#E3F2FD"))
            child.setTextColor(Color.parseColor("#0099FF"))
        }

        // Seleccionar nuevo horario
        button.setBackgroundColor(Color.parseColor("#0099FF"))
        button.setTextColor(Color.WHITE)

        habilitarConfirmacion()
    }

    private fun resetHorarios() {
        fechaSeleccionada = ""
        horaSeleccionada = ""
        gridHorarios.visibility = View.GONE
        txtHorariosTitle.text = "Selecciona una fecha para ver horarios"
        deshabilitarConfirmacion()
    }

    private fun habilitarConfirmacion() {
        if (fechaSeleccionada.isNotEmpty() && horaSeleccionada.isNotEmpty()) {
            btnConfirmar.isEnabled = true
            btnConfirmar.setBackgroundColor(Color.parseColor("#0099FF"))
            btnConfirmar.setTextColor(Color.WHITE)
        }
    }

    private fun deshabilitarConfirmacion() {
        btnConfirmar.isEnabled = false
        btnConfirmar.setBackgroundColor(Color.parseColor("#E0E0E0"))
        btnConfirmar.setTextColor(Color.parseColor("#999"))
    }

    private fun formatearFecha(fecha: String): String {
        val partes = fecha.split("-")
        val dia = partes[0]
        val mes = partes[1].toInt()
        val anio = partes[2]

        val nombresMeses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        return "$dia de ${nombresMeses[mes - 1]} de $anio"
    }

    private fun setupConfirmButton() {
        btnConfirmar.setOnClickListener {
            if (fechaSeleccionada.isNotEmpty() && horaSeleccionada.isNotEmpty()) {
                val intent = Intent(this, confirmacion::class.java)
                intent.putExtra("servicio", findViewById<TextView>(R.id.txtServicio).text)
                intent.putExtra("fecha", formatearFecha(fechaSeleccionada))
                intent.putExtra("hora", horaSeleccionada)
                startActivity(intent)
            }
        }
    }

    // Adapter para los días del calendario
    private inner class DayAdapter(
        private val context: android.content.Context,
        private val days: List<String>
    ) : BaseAdapter() {

        override fun getCount(): Int = days.size

        override fun getItem(position: Int): Any = days[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView = if (convertView == null) {
                TextView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(80, 80)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(Color.parseColor("#333333"))
                }
            } else {
                convertView as TextView
            }

            textView.text = days[position]

            // Estilo para días vacíos
            if (days[position].isEmpty()) {
                textView.setTextColor(Color.TRANSPARENT)
            } else {
                textView.setTextColor(Color.parseColor("#333333"))
            }

            return textView
        }
    }
}