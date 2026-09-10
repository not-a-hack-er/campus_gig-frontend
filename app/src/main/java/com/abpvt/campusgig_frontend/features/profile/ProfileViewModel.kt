/**
 * ProfileViewModel.kt — Manages profile data fetch, update, and logout.
 *
 * OPERATIONS:
 * 1. loadProfile()   — Fetch current user's profile (GET /users/me)
 * 2. updateProfile() — Save edits (PUT /users/me)
 * 3. logout()        — Clear stored token via AuthRepository
 */
package com.abpvt.campusgig_frontend.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import java.io.ByteArrayOutputStream

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    // Current user's profile data
    private val _profile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val profile: StateFlow<Resource<User>> = _profile.asStateFlow()

    // State for profile update operation (null = idle)
    private val _updateState = MutableStateFlow<Resource<User>?>(null)
    val updateState: StateFlow<Resource<User>?> = _updateState.asStateFlow()

    private val _avatarUploadState = MutableStateFlow<Resource<User>?>(null)
    val avatarUploadState: StateFlow<Resource<User>?> = _avatarUploadState.asStateFlow()
    private val _deleteAccountState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteAccountState: StateFlow<Resource<Unit>?> = _deleteAccountState.asStateFlow()

    init {
        loadProfile()
    }

    /** Fetches the logged-in user's own profile from the backend. */
    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = Resource.Loading
            _profile.value = repository.getMyProfile()
        }
    }

    /**
     * Sends updated profile data to the server.
     * On success, also updates the local profile state.
     *
     * @param updatedUser A [User] object with the new values filled in.
     */
    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = repository.updateMyProfile(updatedUser)
            _updateState.value = result
            // If update succeeded, refresh the local profile display
            if (result is Resource.Success<User>) {
                _profile.value = result
            }
        }
    }

    /**
     * Logs the user out by clearing the stored JWT token.
     * The UI should then navigate to the LoginScreen and clear the back stack.
     */
    fun logout() {
        // Access Application-level AuthRepository for logout
        // (ProfileViewModel only knows UserRepository, so we need the app context)
        CampusGigApplication.instance.authRepository.logout()
    }

    /** Reset update state after navigation. */
    fun resetUpdateState() { _updateState.value = null }

    fun uploadAvatar(contentResolver: ContentResolver, uri: Uri) {
        if (_avatarUploadState.value is Resource.Loading) return
        viewModelScope.launch {
            _avatarUploadState.value = Resource.Loading
            val result = try {
                withContext(Dispatchers.IO) {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, info, _ ->
                            val ratio = minOf(1f, 1024f / maxOf(info.size.width, info.size.height))
                            decoder.setTargetSize(maxOf(1, (info.size.width * ratio).toInt()), maxOf(1, (info.size.height * ratio).toInt()))
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    } else {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                        require(options.outWidth > 0 && options.outHeight > 0) { "Unable to read selected photo" }
                        options.inJustDecodeBounds = false
                        options.inSampleSize = 1
                        while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > 1024) options.inSampleSize *= 2
                        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                            ?: error("Unable to read selected photo")
                    }
                    val bytes = try {
                        ByteArrayOutputStream().use { output ->
                            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output))
                            output.toByteArray()
                        }
                    } finally { bitmap.recycle() }
                    repository.uploadAvatar(bytes, "image/jpeg")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Resource.Error("Unable to upload this photo. Please choose another image and try again.")
            }
            _avatarUploadState.value = result
            if (result is Resource.Success<User>) {
                _profile.value = result
            }
        }
    }

    fun resetAvatarUploadState() { _avatarUploadState.value = null }

    fun deleteAccount() {
        if (_deleteAccountState.value is Resource.Loading) return
        viewModelScope.launch {
            _deleteAccountState.value = Resource.Loading
            val result = repository.deleteMyAccount()
            _deleteAccountState.value = result
            if (result is Resource.Success) logout()
        }
    }

    // State for change password operation
    private val _changePasswordState = MutableStateFlow<Resource<String>?>(null)
    val changePasswordState: StateFlow<Resource<String>?> = _changePasswordState.asStateFlow()

    fun changePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading
            _changePasswordState.value = repository.changePassword(currentPass, newPass)
        }
    }

    fun resetChangePasswordState() { _changePasswordState.value = null }
}
