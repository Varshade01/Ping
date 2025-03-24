package com.khrd.pingapp.registration

data class RegistrationFields(
    val name: String,
    val email: String,
    val password: String,
    val consent: Boolean
)