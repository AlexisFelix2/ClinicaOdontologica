package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar el Navigation Drawer
        setupNavigationDrawer()
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
                    // Ya estamos en lobby, solo cerrar drawer
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
                R.id.nav_doctors -> {  // AHORA ESTE ID EXISTE
                    val intent = Intent(this, doctor::class.java)
                    startActivity(intent)
                }
                R.id.nav_perfil_paciente -> {
                    val intent = Intent(this, perfil_paciente::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {  // AHORA ESTE ID EXISTE
                    cerrarSesion()
                }
            }
            true
        }

        // Manejar botón back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun cerrarSesion() {
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