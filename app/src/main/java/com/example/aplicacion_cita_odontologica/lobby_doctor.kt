package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class lobby_doctor : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var txtNombreDoctor: TextView
    private lateinit var txtFechaActual: TextView
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar si hay sesión de doctor
        verificarSesionDoctor()

        // Configurar el Navigation Drawer
        setupNavigationDrawer()

        // Personalizar saludo con nombre del doctor y fecha
        personalizarSaludoDoctor()
        actualizarFechaActual()
        configurarHeaderDoctor()

        // Configurar manejo del botón de retroceso
        configurarBotonRetroceso()
    }

    private fun verificarSesionDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "doctor") {
            // No hay sesión válida de doctor, ir a login
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun personalizarSaludoDoctor() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Doctor")
        val apellidos = prefs.getString("apellidos", "")

        // Actualizar el nombre del doctor en el main layout
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

        // Obtener referencia al header view
        val headerView = navView.getHeaderView(0)

        // Configurar los TextViews del header
        val txtNombreHeader = headerView.findViewById<TextView>(R.id.txtNombreDoctorHeader)
        val txtEspecialidadDoctor = headerView.findViewById<TextView>(R.id.txtEspecialidadDoctor)
        val txtCorreoDoctor = headerView.findViewById<TextView>(R.id.txtCorreoDoctor)

        txtNombreHeader.text = "Dra. $nombre $apellidos"
        txtEspecialidadDoctor.text = especialidad  // ¡ESTA ES LA LÍNEA NUEVA!
        txtCorreoDoctor.text = correo
    }

    private fun configurarBotonRetroceso() {
        // Configurar manejo personalizado del botón de retroceso
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Si el drawer está abierto, cerrarlo
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Mostrar mensaje de "Presione nuevamente para salir" con temporizador
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        // Si presiona dos veces en menos de 2 segundos, minimizar la app
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

        // Formato de fecha en español
        val formato = SimpleDateFormat("EEE, d MMM", Locale("es", "ES"))
        val fechaHoy = formato.format(Date())

        // Capitalizar primera letra
        val fechaFormateada = fechaHoy.substring(0, 1).uppercase() + fechaHoy.substring(1)
        txtFechaActual.text = fechaFormateada
    }

    private fun setupNavigationDrawer() {
        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Configurar botón del menú hamburguesa
        val btnMenu: ImageButton = findViewById(R.id.btnMenu)
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Configurar navegación del drawer
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_lobby_doctor -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_gestionar_citas -> {
                    val intent = Intent(this, gestionar_citas_doctor::class.java)
                    startActivity(intent)
                }
                R.id.nav_pacientes -> {
                    val intent = Intent(this, pacientes_doctor::class.java)
                    startActivity(intent)
                }
                R.id.nav_perfil_doctor -> {
                    val intent = Intent(this, perfil_doctor::class.java)
                    startActivity(intent)
                }
                R.id.nav_horario -> {
                    val intent = Intent(this, horario_doctor::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout_doctor -> {
                    mostrarDialogoCerrarSesion()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        // Por simplicidad para la presentación, cierra directo
        cerrarSesionDoctor()
    }

    private fun cerrarSesionDoctor() {
        // Limpiar SharedPreferences
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

    // Funciones para los botones del grid
    fun irAGestionarCitas(view: View) {
        val intent = Intent(this, gestionar_citas_doctor::class.java)
        startActivity(intent)
    }

    fun irAPacientes(view: View) {
        val intent = Intent(this, pacientes_doctor::class.java)
        startActivity(intent)
    }

    fun irAMiPerfil(view: View) {
        val intent = Intent(this, perfil_doctor::class.java)
        startActivity(intent)
    }

    fun irAHorario(view: View) {
        val intent = Intent(this, horario_doctor::class.java)
        startActivity(intent)
    }
}