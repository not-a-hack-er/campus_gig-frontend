package com.abpvt.campusgig_frontend.data.repository

import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.Gig

class GigRepository(private val api: ApiService) {

    suspend fun getGigs(
        page: Int = 1,
        search: String? = null,
        category: String? = null,
        postedBy: String? = null,
        status: String? = null
    ): Resource<List<Gig>> = safeApiCall {
        api.getGigs(page = page, search = search, category = category, postedBy = postedBy, status = status)
    }

    suspend fun getGigById(id: String): Resource<Gig> = safeApiCall {
        api.getGigById(id)
    }

    suspend fun createGig(gig: Gig): Resource<Gig> = safeApiCall {
        api.createGig(gig)
    }

    suspend fun updateGig(id: String, gig: Gig): Resource<Gig> = safeApiCall {
        api.updateGig(id, gig)
    }

    suspend fun deleteGig(id: String): Resource<String> {
        return try {
            val response = api.deleteGig(id)
            if (response.isSuccessful) Resource.Success(response.body()?.message ?: "Deleted")
            else response.toResourceError()
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun submitWork(id: String, submittedUrl: String, submittedNote: String? = null): Resource<String> {
        return try {
            val body = mutableMapOf("submittedUrl" to submittedUrl)
            if (!submittedNote.isNullOrBlank()) {
                body["submittedNote"] = submittedNote
            }
            val response = api.submitWork(id, body)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.message ?: "Work submitted successfully")
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun getCompletionOtp(id: String): Resource<com.abpvt.campusgig_frontend.data.model.response.CompletionOtpResponse> {
        return try {
            val response = api.getCompletionOtp(id)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) Resource.Success(data)
                else Resource.Error("No OTP data returned")
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun completeGig(id: String, otp: String): Resource<String> {
        return try {
            val response = api.completeGig(id, mapOf("otp" to otp))
            if (response.isSuccessful) {
                Resource.Success(response.body()?.message ?: "Gig completed successfully")
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    /**
     * Generic safe API call wrapper.
     * - Uses safe body() unwrap instead of !! to prevent NullPointerException crashes.
     * - Uses toResourceError() to parse the backend's custom JSON error body.
     */
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
            if (com.abpvt.campusgig_frontend.BuildConfig.DEBUG) {
                android.util.Log.e("GigRepository", "Gig API request failed", e)
            }
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }
}
