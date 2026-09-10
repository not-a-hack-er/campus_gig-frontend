package com.abpvt.campusgig_frontend.core.network

import com.abpvt.campusgig_frontend.data.model.response.ApiResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/** Accept the server's flat payloads and explicit response envelopes. */
class ApiResponseDeserializer : JsonDeserializer<ApiResponse<*>> {
    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): ApiResponse<*> {
        val payloadType = (type as ParameterizedType).actualTypeArguments[0]
        val obj = json.takeIf { it.isJsonObject }?.asJsonObject
        val wrapped = obj?.has("data") == true && obj.has("success")
        val payload = if (wrapped) obj?.get("data") else json
        val data: Any? = if (payload == null || payload.isJsonNull || payloadType == Unit::class.java) null
            else context.deserialize(payload, payloadType)
        return ApiResponse(
            success = obj?.get("success")?.takeUnless { it.isJsonNull }?.asBoolean ?: true,
            message = obj?.get("message")?.takeUnless { it.isJsonNull }?.asString ?: "",
            data = data
        )
    }
}
