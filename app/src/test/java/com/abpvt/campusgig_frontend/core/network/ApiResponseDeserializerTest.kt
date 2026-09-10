package com.abpvt.campusgig_frontend.core.network

import com.abpvt.campusgig_frontend.data.model.response.ApiResponse
import com.abpvt.campusgig_frontend.data.model.response.AvatarUploadData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.*
import org.junit.Test

class ApiResponseDeserializerTest {
    private val gson = GsonBuilder().registerTypeAdapter(ApiResponse::class.java, ApiResponseDeserializer()).create()

    @Test fun flatAvatarResponseContainsUpdatedProfile() {
        val type = object : TypeToken<ApiResponse<AvatarUploadData>>() {}.type
        val result: ApiResponse<AvatarUploadData> = gson.fromJson("""{"avatarUrl":"https://example.com/photo.jpg","user":{"_id":"123","profilePicture":"https://example.com/photo.jpg"}}""", type)
        assertEquals("https://example.com/photo.jpg", result.data?.user?.profilePicture)
    }

    @Test fun acceptsBothFlatAndWrappedOtpResponses() {
        val type = object : TypeToken<ApiResponse<Map<String, String>>>() {}.type
        for (json in listOf("""{"resetToken":"token"}""", """{"success":true,"data":{"resetToken":"token"}}""")) {
            val result: ApiResponse<Map<String, String>> = gson.fromJson(json, type)
            assertEquals("token", result.data?.get("resetToken"))
        }
    }

    @Test fun flatListIsDeserializedForFeedback() {
        val type = object : TypeToken<ApiResponse<List<Map<String, String>>>>() {}.type
        val result: ApiResponse<List<Map<String, String>>> = gson.fromJson("""[{"message":"saved"}]""", type)
        assertEquals("saved", result.data?.first()?.get("message"))
    }
}
