package com.example.aplicacion_cita_odontologica

data class Usuario(
    val dni: String = "",
    val nom_c: String = "",
    val ape_c: String = "",
    val correo_c: String = "",
    val password_c: String = "",
    val telefono: String = "" // Cambié a String para manejo más flexible
)