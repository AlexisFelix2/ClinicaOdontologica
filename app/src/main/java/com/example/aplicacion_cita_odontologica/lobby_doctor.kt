package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class lobby_doctor : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var txtNombreDoctor: TextView
    private lateinit var txtFechaActual: TextView
    private var backPressedTime: Long = 0

    // Vistas para datos dinámicos
    private lateinit var txtPendientesCount: TextView
    private lateinit var txtConfirmadasCount: TextView
    private lateinit var containerCitasHoy: LinearLayout

    private lateinit var db: FirebaseFirestore
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener ID del doctor de la sesión
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        doctorId = prefs.getString("admin_id", null)

        // Verificar si hay sesión de doctor
        verificarSesionDoctor()

        // Inicializar vistas
        initDynamicViews()

        // Configurar el Navigation Drawer
        setupNavigationDrawer()

        // Personalizar saludo con nombre del doctor y fecha
        personalizarSaludoDoctor()
        actualizarFechaActual()
        configurarHeaderDoctor()

        // Configurar manejo del botón de retroceso
        configurarBotonRetroceso()

        // Cargar datos de la BD
        if (doctorId != null) {
            cargarEstadisticasDoctor(doctorId!!)
        } else {
            Toast.makeText(this, "Error: No se pudo obtener el ID del doctor.", Toast.LENGTH_LONG).show()
        }
    }

    private fun initDynamicViews() {
        txtPendientesCount = findViewById(R.id.txtPendientesCount)
        txtConfirmadasCount = findViewById(R.id.txtConfirmadasCount)
        containerCitasHoy = findViewById(R.id.containerCitasHoy)
    }

    private fun cargarEstadisticasDoctor(doctorId: String) {
        db.collection("agendar_cita")
            .whereEqualTo("profesional", doctorId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("LobbyDoctor", "Error al escuchar cambios en citas", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                var pendientes = 0
                var confirmadas = 0
                val citasDeHoy = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

                // Formato de fecha robusto para comparación
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoyStr = format.format(Date())

                for (doc in snapshots) {
                    // Lógica de contadores
                    when (doc.getString("estado")) {
                        "pendiente" -> pendientes++
                        "confirmada" -> confirmadas++
                    }

                    // Lógica para citas de hoy (Método Robusto)
                    val timestamp = doc.getTimestamp("fecha_hora")
                    if (timestamp != null) {
                        val citaStr = format.format(timestamp.toDate())
                        if (hoyStr == citaStr) {
                            val estado = doc.getString("estado")
                            if (estado == "pendiente" || estado == "confirmada") {
                                citasDeHoy.add(doc)
                            }
                        }
                    }
                }

                txtPendientesCount.text = pendientes.toString()
                txtConfirmadasCount.text = confirmadas.toString()

                // Procesar y mostrar citas de hoy
                if (citasDeHoy.isEmpty()) {
                    mostrarMensajeSinCitas()
                } else {
                    // Ordenar por hora
                    citasDeHoy.sortBy { it.getTimestamp("fecha_hora") }
                    val dnis = citasDeHoy.mapNotNull { it.getString("dni_cliente") }.distinct()
                    if (dnis.isNotEmpty()) {
                        db.collection("cliente")
                            .whereIn(FieldPath.documentId(), dnis)
                            .get()
                            .addOnSuccessListener { pacientesSnapshot ->
                                val nombresPacientes = pacientesSnapshot.documents.associate {
                                    val nombre = it.getString("nom_c") ?: ""
                                    val apellido = it.getString("ape_c") ?: ""
                                    it.id to "$nombre $apellido"
                                }
                                actualizarCitasHoyUI(citasDeHoy, nombresPacientes)
                            }
                    } else {
                        mostrarMensajeSinCitas()
                    }
                }
            }
    }

    private fun mostrarMensajeSinCitas() {
        containerCitasHoy.removeAllViews()
        val noCitasTextView = TextView(this).apply {
            text = "No tienes citas programadas para hoy."
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40)
        }
        containerCitasHoy.addView(noCitasTextView)
    }

    private fun actualizarCitasHoyUI(citas: List<com.google.firebase.firestore.DocumentSnapshot>, nombres: Map<String, String>) {
        containerCitasHoy.removeAllViews()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        for (citaDoc in citas) {
            val dni = citaDoc.getString("dni_cliente") ?: continue
            val nombrePaciente = nombres[dni] ?: "Paciente Desconocido"
            val servicio = citaDoc.getString("tipo_servicio") ?: "Servicio no especificado"
            val estado = citaDoc.getString("estado") ?: ""
            val timestamp = citaDoc.getTimestamp("fecha_hora")
            val hora = timestamp?.let { timeFormat.format(it.toDate()) } ?: "--:--"

            val card = crearTarjetaCita(hora, servicio, nombrePaciente, estado)
            containerCitasHoy.addView(card)
        }
    }

    private fun crearTarjetaCita(hora: String, servicio: String, nombrePaciente: String, estado: String): View {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
            radius = 24f
            elevation = 4f
        }

        val outerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER_VERTICAL
        }

        val horaText = TextView(this).apply {
            text = hora
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A1A1A"))
        }

        val lineView = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(8, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                marginStart = 32
                marginEnd = 32
            }
            when (estado.lowercase()) {
                "pendiente" -> setBackgroundColor(Color.parseColor("#FF6B35")) // Orange
                "confirmada" -> setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                else -> setBackgroundColor(Color.GRAY)
            }
        }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val servicioText = TextView(this).apply {
            text = servicio
            setTextColor(Color.parseColor("#333333"))
            setTypeface(null, Typeface.BOLD)
        }

        val pacienteText = TextView(this).apply {
            text = nombrePaciente
            setTextColor(Color.parseColor("#666666"))
        }

        val estadoText = TextView(this).apply {
            text = estado.uppercase()
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }

            when (estado.lowercase()) {
                "pendiente" -> setTextColor(Color.parseColor("#FF6B35"))
                "confirmada" -> setTextColor(Color.parseColor("#4CAF50"))
                else -> setTextColor(Color.GRAY)
            }
        }

        infoLayout.addView(servicioText)
        infoLayout.addView(pacienteText)
        infoLayout.addView(estadoText)

        outerLayout.addView(horaText)
        outerLayout.addView(lineView)
        outerLayout.addView(infoLayout)

        card.addView(outerLayout)
        return card
    }

    private fun verificarSesionDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "doctor") {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun personalizarSaludoDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Doctor")
        val apellidos = prefs.getString("apellidos", "")

        txtNombreDoctor = findViewById(R.id.txtNombreDoctor)
        val saludo = "Dra. $nombre $apellidos"
        txtNombreDoctor.text = saludo
    }

    private fun configurarHeaderDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Doctor")
        val apellidos = prefs.getString("apellidos", "")
        val correo = prefs.getString("correo", "doctor@clinica.com")
        val especialidad = prefs.getString("especialidad", "Odontólogo General")

        val headerView = navView.getHeaderView(0)
        val txtNombreHeader = headerView.findViewById<TextView>(R.id.txtNombreDoctorHeader)
        val txtEspecialidadDoctor = headerView.findViewById<TextView>(R.id.txtEspecialidadDoctor)
        val txtCorreoDoctor = headerView.findViewById<TextView>(R.id.txtCorreoDoctor)

        txtNombreHeader.text = "Dra. $nombre $apellidos"
        txtEspecialidadDoctor.text = especialidad
        txtCorreoDoctor.text = correo
    }

    private fun configurarBotonRetroceso() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        moveTaskToBack(true)
                        finish()
                    } else {
                        Toast.makeText(
                            this@lobby_doctor,
                            "Presiona nuevamente para salir de la aplicación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        })
    }

    private fun actualizarFechaActual() {
        txtFechaActual = findViewById(R.id.txtFechaActual)
        val formato = SimpleDateFormat("EEE, d MMM", Locale("es", "ES"))
        val fechaHoy = formato.format(Date())
        val fechaFormateada = fechaHoy.substring(0, 1).uppercase() + fechaHoy.substring(1)
        txtFechaActual.text = fechaFormateada
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val btnMenu: ImageButton = findViewById(R.id.btnMenu)
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_lobby_doctor -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_gestionar_citas -> abrirGestionarCitas()
                R.id.nav_pacientes -> abrirPacientes()
                R.id.nav_perfil_doctor -> startActivity(Intent(this, perfil_doctor::class.java))
                R.id.nav_logout_doctor -> mostrarDialogoCerrarSesion()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        cerrarSesionDoctor()
    }

    private fun cerrarSesionDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            clear()
            apply()
        }
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun abrirPacientes() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val doctorId = prefs.getString("admin_id", null)
        if (doctorId == null) {
            Toast.makeText(this, "ID de doctor no proporcionado", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, pacientes_doctor::class.java)
        intent.putExtra("doctor_id", doctorId)
        startActivity(intent)
    }

    private fun abrirGestionarCitas() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val doctorId = prefs.getString("admin_id", null)
        if (doctorId == null) {
            Toast.makeText(this, "ID de doctor no proporcionado", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, gestionar_citas_doctor::class.java)
        intent.putExtra("doctor_id", doctorId)
        startActivity(intent)
    }

    fun irAGestionarCitas(view: View) {
        abrirGestionarCitas()
    }

    fun irAPacientes(view: View) {
        abrirPacientes()
    }

    fun irAMiPerfil(view: View) {
        startActivity(Intent(this, perfil_doctor::class.java))
    }

    fun irAHorario(view: View) {
        startActivity(Intent(this, horario_doctor::class.java))
    }
}
