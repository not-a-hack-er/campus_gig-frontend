package com.abpvt.campusgig_frontend.data.repository

import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository(private val api: ApiService) {

    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): Resource<User> {
        return try {
            if (bytes.size > MAX_AVATAR_BYTES) {
                return Resource.Error("Photo is too large. Please choose an image under 5 MB.")
            }
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val extension = when (mimeType.lowercase()) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                else -> "jpg"
            }
            val part = MultipartBody.Part.createFormData("avatar", "profile-photo.$extension", body)
            val response = api.uploadAvatar(part)
            if (response.isSuccessful) {
                val user = response.body()?.data?.user
                    ?: return Resource.Error("Empty profile photo response", response.code())
                Resource.Success(user)
            } else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Profile photo upload failed")
        }
    }

    suspend fun getMyProfile(): Resource<User> {
        return try {
            val response = api.getMyProfile()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: return Resource.Error("Empty profile response", response.code()))
            } else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun deleteMyAccount(): Resource<Unit> = try {
        val response = api.deleteMyAccount(mapOf("confirmation" to "DELETE"))
        if (response.isSuccessful) Resource.Success(Unit) else response.toResourceError()
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Account deletion failed")
    }

    suspend fun updateMyProfile(user: User): Resource<User> {
        return try {
            val response = api.updateMyProfile(user)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: return Resource.Error("Empty profile response", response.code()))
            } else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun getUserById(id: String): Resource<User> {
        return try {
            val response = api.getUserById(id)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: return Resource.Error("User not found", response.code()))
            } else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun changePassword(currentPass: String, newPass: String): Resource<String> {
        return try {
            val response = api.changePassword(
                mapOf("currentPassword" to currentPass, "newPassword" to newPass)
            )
            if (response.isSuccessful) {
                Resource.Success(response.body()?.message ?: "Password changed successfully")
            } else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun updateFcmToken(fcmToken: String): Resource<Unit> {
        return try {
            val response = api.updateFcmToken(mapOf("fcmToken" to fcmToken))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — failed to send FCM token")
        }
    }

    private companion object {
        const val MAX_AVATAR_BYTES = 5 * 1024 * 1024
    }
}
