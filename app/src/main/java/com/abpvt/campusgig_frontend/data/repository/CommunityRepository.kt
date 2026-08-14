package com.abpvt.campusgig_frontend.data.repository

import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.Community

class CommunityRepository(private val api: ApiService) {

    suspend fun getCommunities(): Resource<List<Community>> {
        return safeApiCall { api.getCommunities() }
    }

    suspend fun getCommunityById(id: String): Resource<Community> {
        return safeApiCall { api.getCommunityById(id) }
    }

    suspend fun createCommunity(community: Community): Resource<Community> {
        return safeApiCall { api.createCommunity(community) }
    }

    suspend fun joinCommunity(id: String): Resource<String> {
        return try {
            val response = api.joinCommunity(id)
            if (response.isSuccessful) Resource.Success(response.body()?.message ?: "Joined")
            else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun leaveCommunity(id: String): Resource<String> {
        return try {
            val response = api.leaveCommunity(id)
            if (response.isSuccessful) Resource.Success(response.body()?.message ?: "Left")
            else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Resource.Error("Server returned an empty response.", response.code())
                Resource.Success(body)
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }
}
