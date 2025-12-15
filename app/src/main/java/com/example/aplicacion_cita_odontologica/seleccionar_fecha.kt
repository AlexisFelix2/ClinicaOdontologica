package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class seleccionar_fecha : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var fechaSeleccionada = ""
    private var horaSeleccionada = ""
    private val calendar: Calendar = Calendar.getInstance()

    private lateinit var gridCalendario: GridView
    private lateinit var txtMesAnio: TextView
    private lateinit var gridHorarios: GridLayout
    private lateinit var txtHorariosTitle: TextView
    private lateinit var btnConfirmar: Button

    // Datos de la cita
    private lateinit var tipoServicio: String
    private lateinit var idProfesional: String
    private lateinit var nombreProfesional: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccionar_fecha)

        // Verificar sesión de cliente
        verificarSesionCliente()

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener datos pasados
        tipoServicio = intent.getStringExtra("tipo_servicio") ?: "Servicio"
        idProfesional = intent.getStringExtra("id_profesional") ?: "EC001"
        nombreProfesional = intent.getStringExtra("nombre_profesional") ?: "Doctor"
        val especialidad = intent.getStringExtra("especialidad_profesional") ?: "Odontología General"

        findViewById<TextView>(R.id.txtServicio).text = tipoServicio
        findViewById<TextView>(R.id.txtDoctor).text = "$nombreProfesional - $especialidad"

        initViews()
        setupCalendar()
        setupNavigation()
        setupConfirmButton()
    }

    private fun verificarSesionCliente() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "cliente") {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        gridCalendario = findViewById(R.id.gridCalendario)
        txtMesAnio = findViewById(R.id.txtMesAnio)
        gridHorarios = findViewById(R.id.gridHorarios)
        txtHorariosTitle = findViewById(R.id.txtHorariosTitle)
        btnConfirmar = findViewById(R.id.btnConfirmarHorario)
    }

    /* ---------------- CALENDARIO ---------------- */
    private fun setupCalendar() {
        updateCalendar()

        gridCalendario.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, _, _ ->
                val diaView = view as TextView
                if (diaView.text.isEmpty()) return@OnItemClickListener

                resetCalendarSelection()

                diaView.setBackgroundColor(Color.parseColor("#0099FF"))
                diaView.setTextColor(Color.WHITE)

                val dia = diaView.text.toString().toInt()
                val mes = calendar.get(Calendar.MONTH)
                val anio = calendar.get(Calendar.YEAR)

                fechaSeleccionada = "$dia/${mes + 1}/$anio"
                mostrarHorariosDisponibles(dia, mes, anio)
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
        val format = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val texto = format.format(calendar.time)
        txtMesAnio.text = texto.replaceFirstChar { it.uppercase() }

        gridCalendario.adapter = DayAdapter(this, getDaysInMonth())
    }

    private fun getDaysInMonth(): List<String> {
        val days = mutableListOf<String>()
        val temp = calendar.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)

        val firstDay = temp.get(Calendar.DAY_OF_WEEK)
        val maxDays = temp.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1 until firstDay) days.add("")
        for (i in 1..maxDays) days.add(i.toString())

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

    /* ---------------- HORARIOS ---------------- */
    private fun mostrarHorariosDisponibles(dia: Int, mes: Int, anio: Int) {
        gridHorarios.removeAllViews()

        val calFecha = Calendar.getInstance()
        calFecha.set(anio, mes, dia)

        val diaSemana = calFecha.get(Calendar.DAY_OF_WEEK)

        txtHorariosTitle.text = "Horarios para el ${formatearFecha(fechaSeleccionada)}"
        gridHorarios.visibility = View.VISIBLE

        val horarios = mutableListOf<String>()

        fun rango(inicio: Int, fin: Int) {
            for (h in inicio until fin) {
                horarios.add(String.format("%02d:00", h))
                horarios.add(String.format("%02d:30", h))
            }
        }

        if (diaSemana == Calendar.SATURDAY) {
            rango(8, 12)
            rango(13, 17)
        } else {
            rango(8, 13)
            rango(14, 18)
        }

        for (hora in horarios) {
            val btn = Button(this).apply {
                text = hora
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(12, 12, 12, 12)
                }
                setBackgroundColor(Color.parseColor("#E3F2FD"))
                setTextColor(Color.parseColor("#0099FF"))
                setOnClickListener { seleccionarHorario(hora, this) }
            }
            gridHorarios.addView(btn)
        }
    }

    private fun seleccionarHorario(hora: String, button: Button) {
        horaSeleccionada = hora

        for (i in 0 until gridHorarios.childCount) {
            val b = gridHorarios.getChildAt(i) as Button
            b.setBackgroundColor(Color.parseColor("#E3F2FD"))
            b.setTextColor(Color.parseColor("#0099FF"))
        }

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
        btnConfirmar.isEnabled = true
        btnConfirmar.setBackgroundColor(Color.parseColor("#0099FF"))
        btnConfirmar.setTextColor(Color.WHITE)
    }

    private fun deshabilitarConfirmacion() {
        btnConfirmar.isEnabled = false
        btnConfirmar.setBackgroundColor(Color.parseColor("#E0E0E0"))
        btnConfirmar.setTextColor(Color.parseColor("#999999"))
    }

    /* ---------------- GUARDAR EN FIREBASE ---------------- */
    private fun setupConfirmButton() {
        btnConfirmar.setOnClickListener {
            if (fechaSeleccionada.isNotEmpty() && horaSeleccionada.isNotEmpty()) {
                guardarCitaEnFirebase()
            }
        }
    }

    private fun guardarCitaEnFirebase() {
        // Obtener DNI del cliente logueado
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val dniCliente = prefs.getString("dni", "") ?: ""

        if (dniCliente.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró DNI del cliente", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear timestamp combinando fecha y hora
        val partesFecha = fechaSeleccionada.split("/")
        val dia = partesFecha[0].toInt()
        val mes = partesFecha[1].toInt() - 1  // Calendar.MONTH empieza en 0
        val anio = partesFecha[2].toInt()

        val partesHora = horaSeleccionada.split(":")
        val hora = partesHora[0].toInt()
        val minuto = partesHora[1].toInt()

        val calendarCita = Calendar.getInstance().apply {
            set(anio, mes, dia, hora, minuto, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val timestamp = Timestamp(calendarCita.time)

        // Generar ID único para la cita
        val idCita = "AC${System.currentTimeMillis()}"

        // Crear objeto de cita según los campos requeridos
        val cita = hashMapOf(
            "dni_cliente" to dniCliente,
            "tipo_servicio" to tipoServicio,
            "descripcion" to "Cita agendada por el paciente",
            "fecha_hora" to timestamp,
            "profesional" to idProfesional,  // Solo guardar ID del documento (ej: "EC001")
            "estado" to "pendiente"
        )

        // Guardar en Firebase
        db.collection("agendar_cita")
            .document(idCita)
            .set(cita)
            .addOnSuccessListener {
                // Ir a la pantalla de confirmación
                val intent = Intent(this, confirmacion::class.java)
                intent.putExtra("id_cita", idCita)
                intent.putExtra("tipo_servicio", tipoServicio)
                // Pasar nombre del profesional solo para mostrar en la UI
                intent.putExtra("nombre_profesional", nombreProfesional)
                intent.putExtra("fecha", formatearFecha(fechaSeleccionada))
                intent.putExtra("hora", horaSeleccionada)
                intent.putExtra("estado", "pendiente")
                startActivity(intent)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error al guardar la cita: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatearFecha(fecha: String): String {
        val p = fecha.split("/")
        val meses = arrayOf(
            "Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        )
        return "${p[0]} de ${meses[p[1].toInt() - 1]} de ${p[2]}"
    }

    private inner class DayAdapter(
        val context: android.content.Context,
        val days: List<String>
    ) : BaseAdapter() {

        override fun getCount() = days.size
        override fun getItem(position: Int) = days[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val tv = convertView as? TextView ?: TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(100, 100)
                gravity = android.view.Gravity.CENTER
            }

            tv.text = days[position]
            tv.setTextColor(
                if (days[position].isEmpty()) Color.TRANSPARENT
                else Color.parseColor("#333333")
            )

            return tv
        }
    }

    fun volverAtras(view: View) {
        finish()
    }
}