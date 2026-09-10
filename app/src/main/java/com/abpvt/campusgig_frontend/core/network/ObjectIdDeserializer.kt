package com.abpvt.campusgig_frontend.core.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/** References are IDs in lists and populated objects on detail endpoints. */
class ObjectIdDeserializer : JsonDeserializer<String?> {
    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): String? =
        when {
            json.isJsonNull -> null
            json.isJsonObject -> json.asJsonObject.get("_id")?.asString
            else -> json.asString
        }
}
