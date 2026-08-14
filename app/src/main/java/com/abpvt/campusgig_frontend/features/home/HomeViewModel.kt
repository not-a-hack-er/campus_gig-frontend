/**
 * HomeViewModel.kt — Provides data for the Home dashboard screen.
 *
 * The Home screen is the first thing users see after login — it must feel
 * welcoming and informative. This ViewModel loads:
 *
 * 1. Current user profile  → For the personalized greeting ("Hello, Akash 👋")
 * 2. Recent/featured gigs  → Shows latest opportunities
 * 3. Unread notification count → Badge on the notification bell icon
 *
 * PARALLEL LOADING:
 * All three data loads are launched concurrently using separate coroutines
 * (not sequentially) so the screen loads as fast as possible.
 */
package com.abpvt.campusgig_frontend.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.repository.GigRepository
import com.abpvt.campusgig_frontend.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val gigRepository: GigRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // ── State Flows ───────────────────────────────────────────────────────────
    // Each screen section has its own state flow so they load independently

    /** Current logged-in user — used for personalized greeting and avatar */
    private val _currentUser = MutableStateFlow<Resource<User>>(Resource.Loading)
    val currentUser: StateFlow<Resource<User>> = _currentUser.asStateFlow()

    /** Featured/recent gigs shown on the home feed */
    private val _featuredGigs = MutableStateFlow<Resource<List<Gig>>>(Resource.Loading)
    val featuredGigs: StateFlow<Resource<List<Gig>>> = _featuredGigs.asStateFlow()

    init {
        // Load all data when the ViewModel is first created
        loadHomeData()
    }

    /**
     * Loads all home screen data in parallel.
     * Using separate coroutines (launch blocks) instead of sequential
     * code means all requests fire at the same time.
     */
    fun loadHomeData() {
        // Launch user profile fetch (independent coroutine)
        viewModelScope.launch {
            if (_currentUser.value !is Resource.Success) {
                _currentUser.value = Resource.Loading
            }
            _currentUser.value = userRepository.getMyProfile()
        }

        // Launch gig list fetch (independent coroutine — runs at same time)
        viewModelScope.launch {
            if (_featuredGigs.value !is Resource.Success) {
                _featuredGigs.value = Resource.Loading
            }
            // Fetch first page of gigs with default sorting (newest first from backend)
            _featuredGigs.value = gigRepository.getGigs(page = 1)
        }
    }

    /**
     * Pull-to-refresh: user manually refreshes the home feed.
     * Same as loadHomeData() but gives explicit user control.
     */
    fun refresh() = loadHomeData()
}
