package com.abpvt.campusgig_frontend.data.model.response

import com.abpvt.campusgig_frontend.data.model.User

data class AuthResponse(
    val token: String,
    val user: User
)
