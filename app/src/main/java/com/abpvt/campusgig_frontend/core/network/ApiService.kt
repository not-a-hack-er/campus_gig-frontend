package com.abpvt.campusgig_frontend.core.network

import com.abpvt.campusgig_frontend.data.model.Application
import com.abpvt.campusgig_frontend.data.model.Community
import com.abpvt.campusgig_frontend.data.model.ConversationItem
import com.abpvt.campusgig_frontend.data.model.Feedback
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.data.model.Message
import com.abpvt.campusgig_frontend.data.model.Notification
import com.abpvt.campusgig_frontend.data.model.Post
import com.abpvt.campusgig_frontend.data.model.Review
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.model.request.FeedbackRequest
import com.abpvt.campusgig_frontend.data.model.request.LoginRequest
import com.abpvt.campusgig_frontend.data.model.request.RegisterRequest
import com.abpvt.campusgig_frontend.data.model.response.ApiResponse
import com.abpvt.campusgig_frontend.data.model.response.AuthResponse
import com.abpvt.campusgig_frontend.data.model.response.CompletionOtpResponse
import com.abpvt.campusgig_frontend.data.model.response.MessageResponse
import com.abpvt.campusgig_frontend.data.model.response.UnreadCountResponse
import com.abpvt.campusgig_frontend.data.model.response.VerifyOtpResponse
import com.abpvt.campusgig_frontend.data.model.response.AvatarUploadData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ─── Auth ───────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<okhttp3.ResponseBody>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<okhttp3.ResponseBody>

    @POST("auth/google")
    suspend fun googleLogin(@Body body: Map<String, String>): Response<okhttp3.ResponseBody>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): Response<ApiResponse<VerifyOtpResponse>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    // ─── User / Profile ──────────────────────────────────────────────────────
    @GET("users/me")
    suspend fun getMyProfile(): Response<User>

    @PUT("users/me")
    suspend fun updateMyProfile(@Body body: User): Response<User>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse<AvatarUploadData>>

    @POST("users/me/fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    @POST("users/me/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>

    // ─── Gigs ────────────────────────────────────────────────────────────────
    @GET("gigs")
    suspend fun getGigs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("postedBy") postedBy: String? = null,
        @Query("status") status: String? = null
    ): Response<List<Gig>>

    @GET("gigs/{id}")
    suspend fun getGigById(@Path("id") id: String): Response<Gig>

    @POST("gigs")
    suspend fun createGig(@Body body: Gig): Response<Gig>

    @PUT("gigs/{id}")
    suspend fun updateGig(@Path("id") id: String, @Body body: Gig): Response<Gig>

    @DELETE("gigs/{id}")
    suspend fun deleteGig(@Path("id") id: String): Response<MessageResponse>

    @POST("gigs/{id}/submit-work")
    suspend fun submitWork(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @GET("gigs/{id}/completion-otp")
    suspend fun getCompletionOtp(@Path("id") id: String): Response<ApiResponse<CompletionOtpResponse>>

    @POST("gigs/{id}/complete")
    suspend fun completeGig(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    // ─── Applications ────────────────────────────────────────────────────────
    @GET("applications/my")
    suspend fun getMyApplications(): Response<List<Application>>

    @GET("applications/gig/{gigId}")
    suspend fun getApplicationsForGig(@Path("gigId") gigId: String): Response<List<Application>>

    @POST("applications/{gigId}")
    suspend fun applyForGig(
        @Path("gigId") gigId: String,
        @Body body: Map<String, String>
    ): Response<Application>

    @PUT("applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Application>

    // ─── Chat ────────────────────────────────────────────────────────────────
    // NOTE: /inbox must be a separate named path (not a path param) to avoid
    // ambiguity with the GET messages/{receiverId} endpoint on the backend.
    @GET("messages/{receiverId}")
    suspend fun getMessages(
        @Path("receiverId") receiverId: String,
        @Query("gigId") gigId: String
    ): Response<List<Message>>

    @PUT("messages/{receiverId}/read")
    suspend fun markMessagesRead(
        @Path("receiverId") receiverId: String,
        @Query("gigId") gigId: String
    ): Response<MessageResponse>

    @GET("messages/inbox")
    suspend fun getInbox(): Response<List<ConversationItem>>

    // ─── Communities ─────────────────────────────────────────────────────────
    @GET("communities")
    suspend fun getCommunities(): Response<List<Community>>

    @GET("communities/{id}")
    suspend fun getCommunityById(@Path("id") id: String): Response<Community>

    @POST("communities")
    suspend fun createCommunity(@Body body: Community): Response<Community>

    @POST("communities/{id}/join")
    suspend fun joinCommunity(@Path("id") id: String): Response<MessageResponse>

    @POST("communities/{id}/leave")
    suspend fun leaveCommunity(@Path("id") id: String): Response<MessageResponse>

    @GET("communities/{id}/feed")
    suspend fun getCommunityFeed(@Path("id") id: String): Response<List<Post>>

    @POST("communities/{id}/posts")
    suspend fun createCommunityPost(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Post>

    // ─── Notifications ───────────────────────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<Notification>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<MessageResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<ApiResponse<UnreadCountResponse>>

    // ─── Reviews ─────────────────────────────────────────────────────────────
    @GET("reviews/user/{userId}")
    suspend fun getReviewsForUser(@Path("userId") userId: String): Response<List<Review>>

    // One review per (reviewer, gig). Body sends rating, comment, gigId.
    @POST("reviews/{userId}")
    suspend fun submitReview(
        @Path("userId") userId: String,
        @Body body: Map<String, Any?>
    ): Response<ApiResponse<Review>>

    // ─── Feedback ────────────────────────────────────────────────────────────
    @POST("feedback")
    suspend fun submitFeedback(@Body body: FeedbackRequest): Response<ApiResponse<Feedback>>

    @GET("feedback/my")
    suspend fun getMyFeedback(): Response<ApiResponse<List<Feedback>>>
}
