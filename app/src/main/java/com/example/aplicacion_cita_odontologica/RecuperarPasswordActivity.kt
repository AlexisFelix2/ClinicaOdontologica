package com.example.aplicacion_cita_odontologica

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class RecuperarPasswordActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var inputCorreo: EditText
    private lateinit var inputDni: EditText
    private lateinit var inputNuevaPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_password)

        db = FirebaseFirestore.getInstance()

        inputCorreo = findViewById(R.id.inputCorreo)
        inputDni = findViewById(R.id.inputDni)
        inputNuevaPassword = findViewById(R.id.inputNuevaPassword)
    }

    fun volverAtras(view: View) {
        finish()
    }

    fun cambiarPassword(view: View) {
        val correo = inputCorreo.text.toString().trim()
        val dni = inputDni.text.toString().trim().uppercase()
        val nuevaPassword = inputNuevaPassword.text.toString().trim()

        if (correo.isEmpty() || dni.isEmpty() || nuevaPassword.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevaPassword.length < 6) {
            inputNuevaPassword.error = "Mínimo 6 caracteres"
            return
        }

        db.collection("cliente")
            .document(dni)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "DNI no encontrado", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val correoBD = doc.getString("correo_c") ?: ""

                if (correoBD != correo) {
                    Toast.makeText(this, "Correo no coincide con el DNI", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                db.collection("cliente")
                    .document(dni)
                    .update("password_c", nuevaPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_LONG).show()
                    }
            }
    }
}
