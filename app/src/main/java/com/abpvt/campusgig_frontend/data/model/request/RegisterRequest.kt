package com.abpvt.campusgig_frontend.data.model.request

/**
 * RegisterRequest — payload sent to POST /auth/register.
 *
 * All Step 1 AND Step 2 registration form fields are included here.
 * Previously, course/yearOfStudy/skills were collected in the UI but never sent!
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,           // "student" | "employer"
    val college: String,
    val branch: String = "",    // Course / Program (e.g. "Computer Science")
    val yearOfStudy: String = "",// e.g. "1st", "2nd", "3rd", "4th", "5th+"
    val skills: List<String> = emptyList() // e.g. ["Kotlin", "Android", "Figma"]
)
