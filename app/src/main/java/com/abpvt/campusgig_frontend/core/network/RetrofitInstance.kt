package com.abpvt.campusgig_frontend.core.network

import android.content.Context
import com.abpvt.campusgig_frontend.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit instance.
 * Call [RetrofitInstance.create] with the application [Context] to get an [ApiService].
 *
 * PRODUCTION SAFETY:
 * - BASE_URL is injected from BuildConfig (set via keystore.properties or CI), never hardcoded.
 * - HTTP logging is DISABLED in release builds to prevent token/credential leakage in Logcat.
 */
object RetrofitInstance {

    /**
     * Security: Only log full request/response body in debug builds.
     * In production (release), logging level is NONE to prevent JWT tokens,
     * passwords, and personal data from appearing in system Logcat.
     */
    private fun getLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private fun getOkHttpClient(context: Context) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(context))
        .addInterceptor(getLoggingInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .setLenient()
        .create()

    fun create(context: Context): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)   // Injected at compile-time via keystore.properties
            .client(getOkHttpClient(context.applicationContext))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

