package com.abpvt.campusgig_frontend.features.applications
import androidx.compose.material3.MaterialTheme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApplicationViewModel(private val api: ApiService) : ViewModel() {

    private val _applications = MutableStateFlow<Resource<List<Application>>>(Resource.Loading)
    val applications: StateFlow<Resource<List<Application>>> = _applications

    private val _applyState = MutableStateFlow<Resource<Application>?>(null)
    val applyState: StateFlow<Resource<Application>?> = _applyState

    /** True while an accept/reject API call is in-flight — used to show a spinner */
    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress

    /** Last error message from an action — consumed by UI to show a Toast */
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError

    /** Last success message from withdrawal */
    private val _withdrawSuccess = MutableStateFlow<String?>(null)
    val withdrawSuccess: StateFlow<String?> = _withdrawSuccess

    fun clearActionError() { _actionError.value = null }
    fun clearWithdrawSuccess() { _withdrawSuccess.value = null }

    init {
        loadMyApplications()
    }

    fun loadMyApplications() {
        viewModelScope.launch {
            if (_applications.value !is Resource.Success) {
                _applications.value = Resource.Loading
            }
            try {
                val response = api.getMyApplications()
                _applications.value = if (response.isSuccessful)
                    Resource.Success(response.body()!!)
                else
                    response.toResourceError()
            } catch (e: Exception) {
                _applications.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun applyForGig(gigId: String, proposal: String, expectedBudget: Double) {
        viewModelScope.launch {
            _applyState.value = Resource.Loading
            try {
                val body = mapOf(
                    "proposal" to proposal,
                    "expectedBudget" to expectedBudget.toString()
                )
                val response = api.applyForGig(gigId, body)
                _applyState.value = if (response.isSuccessful)
                    Resource.Success(response.body()!!)
                else
                    response.toResourceError()
            } catch (e: Exception) {
                _applyState.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun withdrawApplication(applicationId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _actionInProgress.value = true
            _actionError.value = null
            try {
                val response = api.updateApplicationStatus(applicationId, mapOf("status" to "withdrawn"))
                if (response.isSuccessful) {
                    _withdrawSuccess.value = "Application withdrawn successfully"
                    loadMyApplications()
                    onComplete(true)
                } else {
                    val err = response.toResourceError()
                    _actionError.value = err.message
                    onComplete(false)
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Error withdrawing application"
                _actionError.value = msg
                onComplete(false)
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    fun resetApplyState() { _applyState.value = null }

    private val _gigApplications = MutableStateFlow<Resource<List<Application>>>(Resource.Loading)
    val gigApplications: StateFlow<Resource<List<Application>>> = _gigApplications

    fun loadApplicationsForGig(gigId: String) {
        viewModelScope.launch {
            _gigApplications.value = Resource.Loading
            try {
                val response = api.getApplicationsForGig(gigId)
                _gigApplications.value = if (response.isSuccessful)
                    Resource.Success(response.body()!!)
                else
                    response.toResourceError()
            } catch (e: java.lang.Exception) {
                _gigApplications.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    /**
     * Accept / Reject / Withdraw an application.
     *
     * @param onComplete callback: true on success, false on failure.
     *   On failure, actionError is also set for Toast display.
     */
    fun updateApplicationStatus(applicationId: String, status: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _actionInProgress.value = true
            _actionError.value = null
            try {
                Log.d("ApplicationVM", "Updating application $applicationId to status=$status")
                val response = api.updateApplicationStatus(applicationId, mapOf("status" to status))
                if (response.isSuccessful) {
                    Log.d("ApplicationVM", "Status update SUCCESS for $applicationId")
                    onComplete(true)
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    Log.e("ApplicationVM", "Status update FAILED: code=${response.code()} body=$errorBody")
                    _actionError.value = "Failed: $errorBody"
                    onComplete(false)
                }
            } catch (e: java.lang.Exception) {
                Log.e("ApplicationVM", "Status update EXCEPTION", e)
                _actionError.value = e.localizedMessage ?: "Network error"
                onComplete(false)
            } finally {
                _actionInProgress.value = false
            }
        }
    }
}
