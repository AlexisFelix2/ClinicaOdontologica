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

class lobby : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var txtSaludo: TextView
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar si hay sesión de cliente
        verificarSesionCliente()

        // Configurar el Navigation Drawer
        setupNavigationDrawer()

        // Personalizar saludo con nombre del usuario
        personalizarSaludo()
        configurarHeaderPaciente()

        // Configurar manejo del botón de retroceso
        configurarBotonRetroceso()
    }

    private fun verificarSesionCliente() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val logueado = prefs.getBoolean("logueado", false)
        val tipoUsuario = prefs.getString("tipo_usuario", "")

        if (!logueado || tipoUsuario != "cliente") {
            // No hay sesión válida de cliente, ir a login
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun personalizarSaludo() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Usuario")

        // Buscar el TextView de saludo en el main layout
        txtSaludo = findViewById(R.id.txtSaludo)
        txtSaludo.text = "Hola, $nombre"
    }

    private fun configurarHeaderPaciente() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Paciente")
        val apellidos = prefs.getString("apellidos", "")
        val correo = prefs.getString("correo", "correo@ejemplo.com")
        val dni = prefs.getString("dni", "00000000")

        // Obtener referencia al header view
        val headerView = navView.getHeaderView(0)

        // Configurar los TextViews del header
        val txtNombreHeader = headerView.findViewById<TextView>(R.id.txtNombrePacienteHeader)
        val txtCorreoPaciente = headerView.findViewById<TextView>(R.id.txtCorreoPaciente)
        val txtDNIPaciente = headerView.findViewById<TextView>(R.id.txtDNIPaciente)

        txtNombreHeader.text = "$nombre $apellidos"
        txtCorreoPaciente.text = correo
        txtDNIPaciente.text = "DNI: $dni"
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
                            this@lobby,
                            "Presiona nuevamente para salir de la aplicación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        })
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
                R.id.nav_lobby -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_mis_citas -> {
                    val intent = Intent(this, miscitas::class.java)
                    startActivity(intent)
                }
                R.id.nav_servicios -> {
                    val intent = Intent(this, servicios::class.java)
                    startActivity(intent)
                }
                R.id.nav_doctors -> {
                    val intent = Intent(this, doctor::class.java)
                    startActivity(intent)
                }
                R.id.nav_perfil_paciente -> {
                    val intent = Intent(this, perfil_paciente::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    mostrarDialogoCerrarSesion()
                }
            }
            true
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        // Puedes usar un AlertDialog para confirmar
        // Por simplicidad, lo haré directo para tu presentación
        cerrarSesion()
    }

    private fun cerrarSesion() {
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
    fun irAServicios(view: View) {
        val intent = Intent(this, servicios::class.java)
        startActivity(intent)
    }

    fun irAMisCitas(view: View) {
        val intent = Intent(this, miscitas::class.java)
        startActivity(intent)
    }

    fun irAMiPerfil(view: View) {
        val intent = Intent(this, perfil_paciente::class.java)
        startActivity(intent)
    }

    fun verDetallesCita(view: View) {
        val intent = Intent(this, miscitas::class.java)
        startActivity(intent)
    }
}