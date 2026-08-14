package com.abpvt.campusgig_frontend.data.repository

import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.User

class UserRepository(private val api: ApiService) {

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
}
