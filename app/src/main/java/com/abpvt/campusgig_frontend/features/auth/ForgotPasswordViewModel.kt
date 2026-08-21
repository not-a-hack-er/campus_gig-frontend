package com.abpvt.campusgig_frontend.features.auth
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    ENTER_EMAIL,
    VERIFY_OTP,
    NEW_PASSWORD,
    SUCCESS
}

class ForgotPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _currentStep = MutableStateFlow(ForgotPasswordStep.ENTER_EMAIL)
    val currentStep: StateFlow<ForgotPasswordStep> = _currentStep.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _resetToken = MutableStateFlow("")
    val resetToken: StateFlow<String> = _resetToken.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<String>?>(null)
    val actionState: StateFlow<Resource<String>?> = _actionState.asStateFlow()

    fun setEmail(value: String) {
        _email.value = value
    }

    fun requestOtp(emailInput: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _email.value = emailInput.trim()
            val result = repository.forgotPassword(emailInput.trim())
            _actionState.value = result
            if (result is Resource.Success) {
                _currentStep.value = ForgotPasswordStep.VERIFY_OTP
            }
        }
    }

    fun verifyOtp(otpInput: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = repository.verifyOtp(_email.value, otpInput.trim())
            if (result is Resource.Success) {
                _resetToken.value = result.data
                _actionState.value = Resource.Success("OTP verified successfully")
                _currentStep.value = ForgotPasswordStep.NEW_PASSWORD
            } else if (result is Resource.Error) {
                _actionState.value = result
            }
        }
    }

    fun resetPassword(newPassword: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = repository.resetPassword(_resetToken.value, newPassword)
            _actionState.value = result
            if (result is Resource.Success) {
                _currentStep.value = ForgotPasswordStep.SUCCESS
            }
        }
    }

    fun resendOtp() {
        if (_email.value.isNotBlank()) {
            requestOtp(_email.value)
        }
    }

    fun clearState() {
        _actionState.value = null
    }

    fun goToStep(step: ForgotPasswordStep) {
        _currentStep.value = step
        _actionState.value = null
    }
}
