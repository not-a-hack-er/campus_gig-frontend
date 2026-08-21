/**
 * GigViewModel.kt — Manages all gig-related state and operations.
 *
 * This ViewModel serves THREE different screens:
 * 1. GigListScreen   — Browse and search all gigs
 * 2. GigDetailScreen — View a single gig's full details
 * 3. CreateGigScreen — Post a new gig
 *
 * Each screen has its own StateFlow so they don't interfere with each other.
 * For example, loading gig details doesn't affect the gig list.
 *
 * SEARCH & FILTER:
 * The loadGigs() function accepts optional search text and category filter.
 * These are passed as query parameters to the Retrofit API call.
 * The backend returns filtered results — we don't filter client-side
 * (avoids loading all gigs and filtering locally).
 */
package com.abpvt.campusgig_frontend.features.gigs
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.data.repository.GigRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GigViewModel(private val repository: GigRepository) : ViewModel() {

    // ── Gig List State ────────────────────────────────────────────────────────
    private val _gigs = MutableStateFlow<Resource<List<Gig>>>(Resource.Loading)
    val gigs: StateFlow<Resource<List<Gig>>> = _gigs.asStateFlow()

    // ── Single Gig Detail State ───────────────────────────────────────────────
    private val _selectedGig = MutableStateFlow<Resource<Gig>?>(null)
    val selectedGig: StateFlow<Resource<Gig>?> = _selectedGig.asStateFlow()

    // ── Create Gig State ──────────────────────────────────────────────────────
    private val _createState = MutableStateFlow<Resource<Gig>?>(null)
    val createState: StateFlow<Resource<Gig>?> = _createState.asStateFlow()

    // ── My Posted Gigs State ──────────────────────────────────────────────────
    private val _myGigs = MutableStateFlow<Resource<List<Gig>>>(Resource.Loading)
    val myGigs: StateFlow<Resource<List<Gig>>> = _myGigs.asStateFlow()

    fun loadMyGigs(userId: String) {
        viewModelScope.launch {
            if (_myGigs.value !is Resource.Success) {
                _myGigs.value = Resource.Loading
            }
            _myGigs.value = repository.getGigs(postedBy = userId, status = "ALL")
        }
    }

    // Track current search params to support refresh
    private var currentSearch: String? = null
    private var currentCategory: String? = null

    /**
     * Job for debounced search — cancels the previous search if the user
     * keeps typing. Only fires the API call after 400ms of inactivity.
     * This prevents a new request on every single keystroke.
     */
    private var searchJob: Job? = null

    init {
        loadGigs()  // Load all gigs immediately when ViewModel is created
    }

    /**
     * Loads/refreshes the gig list with optional filters.
     *
     * @param search   Text to search in gig title/description (null = no filter)
     * @param category Category filter from Constants.GIG_CATEGORIES (null = all)
     * @param debounce If true, waits 400ms before firing API (use for search-as-you-type)
     */
    fun loadGigs(search: String? = null, category: String? = null, debounce: Boolean = false) {
        // Save params for refresh
        currentSearch = search
        currentCategory = category

        // Cancel previous debounced search if still waiting
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            if (debounce) {
                delay(400)  // Wait 400ms before firing API to avoid rapid-fire requests
            }
            if (_gigs.value !is Resource.Success) {
                _gigs.value = Resource.Loading
            }
            _gigs.value = repository.getGigs(
                page = 1,
                search = search?.takeIf { it.isNotBlank() },
                category = category?.takeIf { it != "All" }  // "All" means no category filter
            )
        }
    }

    /**
     * Loads a single gig's full details by its ID.
     * Used when navigating to the GigDetailScreen.
     *
     * @param id The MongoDB _id of the gig
     */
    fun loadGigById(id: String) {
        viewModelScope.launch {
            _selectedGig.value = Resource.Loading
            _selectedGig.value = repository.getGigById(id)
        }
    }

    /**
     * Creates a new gig posting.
     * On success, the CreateGigScreen navigates back to the gig list.
     *
     * @param gig The [Gig] object with all form data filled in
     */
    fun createGig(gig: Gig) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            _createState.value = repository.createGig(gig)
            // Refresh the gig list so the new gig appears immediately
            if (_createState.value is Resource.Success) {
                loadGigs()
            }
        }
    }

    /**
     * Deletes a gig (only the employer who posted it can delete it).
     * The backend enforces authorization.
     *
     * @param id        The gig's MongoDB _id
     * @param onSuccess Called after successful deletion (typically navigate back)
     */
    fun deleteGig(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteGig(id)
            if (result is Resource.Success) {
                onSuccess()
                loadGigs()  // Refresh list to remove the deleted gig
            }
        }
    }

    /** Resets the create state to null (called after navigation on success). */
    fun resetCreateState() { _createState.value = null }

    /** Clears the selected gig when navigating away from the detail screen. */
    fun clearSelectedGig() { _selectedGig.value = null }
}
