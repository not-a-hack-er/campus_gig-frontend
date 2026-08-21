package com.abpvt.campusgig_frontend.data.model.response

data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String = "",
    val data: T? = null
)
