package com.abpvt.campusgig_frontend.data.repository

import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.Feedback
import com.abpvt.campusgig_frontend.data.model.request.FeedbackRequest

class FeedbackRepository(private val api: ApiService) {

    suspend fun submitFeedback(
        type: String,
        rating: Int?,
        message: String,
        deviceInfo: String = "Android App v3.0"
    ): Resource<Feedback> {
        return try {
            val req = FeedbackRequest(type = type, rating = rating, message = message, deviceInfo = deviceInfo)
            val response = api.submitFeedback(req)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) Resource.Success(data)
                else Resource.Error(response.body()?.message ?: "Feedback submitted successfully")
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun getMyFeedback(): Resource<List<Feedback>> {
        return try {
            val response = api.getMyFeedback()
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                Resource.Success(data)
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }
}
