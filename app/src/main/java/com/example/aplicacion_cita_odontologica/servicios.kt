package com.example.aplicacion_cita_odontologica

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class servicios : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_servicios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarBuscador()
    }

    // ================= BUSCADOR =================
    private fun configurarBuscador() {
        val buscador = findViewById<EditText>(R.id.etBuscarServicio)

        val cardLimpieza = findViewById<MaterialCardView>(R.id.cardLimpieza)
        val cardRevision = findViewById<MaterialCardView>(R.id.cardRevision)
        val cardOrtodoncia = findViewById<MaterialCardView>(R.id.cardOrtodoncia)
        val cardBlanqueamiento = findViewById<MaterialCardView>(R.id.cardBlanqueamiento)
        val cardImplantes = findViewById<MaterialCardView>(R.id.cardImplantes)

        buscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().lowercase()

                cardLimpieza.visibility =
                    if ("limpieza".contains(texto)) View.VISIBLE else View.GONE

                cardRevision.visibility =
                    if ("revisión".contains(texto) || "revision".contains(texto)) View.VISIBLE else View.GONE

                cardOrtodoncia.visibility =
                    if ("ortodoncia".contains(texto)) View.VISIBLE else View.GONE

                cardBlanqueamiento.visibility =
                    if ("blanqueamiento".contains(texto)) View.VISIBLE else View.GONE

                cardImplantes.visibility =
                    if ("implantes".contains(texto)) View.VISIBLE else View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ================= NAVEGACIÓN =================
    fun volverALobby(view: View) {
        finish()
    }

    fun seleccionarLimpieza(view: View) {
        startActivity(Intent(this, seleccionar_fecha::class.java)
            .putExtra("servicio", "Limpieza Dental Profesional"))
    }

    fun seleccionarRevision(view: View) {
        startActivity(Intent(this, seleccionar_fecha::class.java)
            .putExtra("servicio", "Revisión General"))
    }

    fun seleccionarOrtodoncia(view: View) {
        startActivity(Intent(this, seleccionar_fecha::class.java)
            .putExtra("servicio", "Ortodoncia"))
    }

    fun seleccionarBlanqueamiento(view: View) {
        startActivity(Intent(this, seleccionar_fecha::class.java)
            .putExtra("servicio", "Blanqueamiento Dental"))
    }

    fun seleccionarImplantes(view: View) {
        startActivity(Intent(this, seleccionar_fecha::class.java)
            .putExtra("servicio", "Implantes Dentales"))
    }

    fun noSeQueNecesito(view: View) {
        finish()
    }
}
