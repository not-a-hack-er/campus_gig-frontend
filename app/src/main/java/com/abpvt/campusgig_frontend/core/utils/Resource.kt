package com.abpvt.campusgig_frontend.core.utils

/**
 * A generic sealed class to wrap API/repository results with state.
 * Use in ViewModels and repositories to represent Loading, Success, or Error states.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError get() = this is Error
}

/**
 * Extension function on Retrofit Response to convert it to Resource.Error with populated message
 * from JSON error body (e.g. {"message": "Descriptive message"}), falling back to response.message().
 */
fun <T> retrofit2.Response<T>.toResourceError(): Resource.Error {
    val defaultMsg = when (code()) {
        400 -> "Bad Request — Invalid parameters"
        401 -> "Unauthorized — Please log in again"
        403 -> "Forbidden — You don't have access to this resource"
        404 -> "Not Found — Requested resource does not exist"
        409 -> "Conflict — Resource already exists"
        500 -> "Server Error — Something went wrong on our servers"
        else -> this.message().ifBlank { "HTTP Error ${code()}" }
    }
    val msg = try {
        val errorStr = errorBody()?.string()
        if (!errorStr.isNullOrBlank() && errorStr.trim().startsWith("{")) {
            val json = org.json.JSONObject(errorStr)
            val parsedMsg = json.optString("message", "")
            if (parsedMsg.isNotBlank()) parsedMsg else defaultMsg
        } else {
            defaultMsg
        }
    } catch (e: Exception) {
        defaultMsg
    }
    return Resource.Error(msg, code())
}
