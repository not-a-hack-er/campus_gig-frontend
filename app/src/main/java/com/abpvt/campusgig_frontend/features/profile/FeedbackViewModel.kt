package com.abpvt.campusgig_frontend.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Feedback
import com.abpvt.campusgig_frontend.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(private val repository: FeedbackRepository) : ViewModel() {

    private val _submitState = MutableStateFlow<Resource<Feedback>?>(null)
    val submitState: StateFlow<Resource<Feedback>?> = _submitState.asStateFlow()

    private val _myFeedbackState = MutableStateFlow<Resource<List<Feedback>>>(Resource.Loading)
    val myFeedbackState: StateFlow<Resource<List<Feedback>>> = _myFeedbackState.asStateFlow()

    fun submitFeedback(
        type: String,
        rating: Int?,
        message: String,
        deviceInfo: String = "Android App v3.0",
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading
            val result = repository.submitFeedback(type, rating, message, deviceInfo)
            _submitState.value = result
            if (result is Resource.Success) {
                onSuccess?.invoke()
            }
        }
    }

    fun loadMyFeedback() {
        viewModelScope.launch {
            _myFeedbackState.value = Resource.Loading
            _myFeedbackState.value = repository.getMyFeedback()
        }
    }

    fun resetSubmitState() {
        _submitState.value = null
    }
}
