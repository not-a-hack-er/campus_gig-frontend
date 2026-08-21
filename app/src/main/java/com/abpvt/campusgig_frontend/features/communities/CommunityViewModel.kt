package com.abpvt.campusgig_frontend.features.communities
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.Constants
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Community
import com.abpvt.campusgig_frontend.data.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(private val repository: CommunityRepository) : ViewModel() {

    private val _communities = MutableStateFlow<Resource<List<Community>>>(Resource.Loading)
    val communities: StateFlow<Resource<List<Community>>> = _communities

    private val _selectedCommunity = MutableStateFlow<Resource<Community>?>(null)
    val selectedCommunity: StateFlow<Resource<Community>?> = _selectedCommunity

    private val _isMember = MutableStateFlow(false)
    val isMember: StateFlow<Boolean> = _isMember.asStateFlow()

    private val _createState = MutableStateFlow<Resource<Community>?>(null)
    val createState: StateFlow<Resource<Community>?> = _createState

    private val _actionState = MutableStateFlow<Resource<String>?>(null)
    val actionState: StateFlow<Resource<String>?> = _actionState

    init { loadCommunities() }

    fun loadCommunities() {
        viewModelScope.launch {
            _communities.value = Resource.Loading
            _communities.value = repository.getCommunities()
        }
    }

    fun loadCommunityById(id: String) {
        viewModelScope.launch {
            _selectedCommunity.value = Resource.Loading
            val res = repository.getCommunityById(id)
            _selectedCommunity.value = res
            updateMemberState(res)
        }
    }

    private fun updateMemberState(res: Resource<Community>) {
        if (res is Resource.Success) {
            val myId = CampusGigApplication.instance.getSharedPreferences(Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getString(Constants.KEY_USER_ID, null)
            _isMember.value = res.data.members.any { it.id == myId }
        } else {
            _isMember.value = false
        }
    }

    fun createCommunity(community: Community) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            _createState.value = repository.createCommunity(community)
        }
    }

    fun joinCommunity(id: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val res = repository.joinCommunity(id)
            _actionState.value = res
            if (res is Resource.Success) {
                _isMember.value = true
                loadCommunityById(id)
            }
        }
    }

    fun leaveCommunity(id: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val res = repository.leaveCommunity(id)
            _actionState.value = res
            if (res is Resource.Success) {
                _isMember.value = false
                loadCommunityById(id)
            }
        }
    }

    fun resetCreateState() { _createState.value = null }
    fun resetActionState() { _actionState.value = null }
}

