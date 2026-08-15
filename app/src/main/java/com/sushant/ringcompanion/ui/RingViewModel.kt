package com.sushant.ringcompanion.ui

import android.app.Application
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sushant.ringcompanion.data.HealthRepository
import com.sushant.ringcompanion.data.RingSnapshot
import com.sushant.ringcompanion.data.SampleRingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RingUiState(
    val snapshot: RingSnapshot = SampleRingData.snapshot,
    val loading: Boolean = false,
    val connected: Boolean = false,
    val message: String? = null
)

class RingViewModel(app: Application) : AndroidViewModel(app) {
    val repo = HealthRepository(app)
    private val _state = MutableStateFlow(RingUiState())
    val state: StateFlow<RingUiState> = _state

    val permissionContract = PermissionController.createRequestPermissionResultContract()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            val snap = runCatching { repo.loadSnapshot() }.getOrElse { SampleRingData.snapshot }
            val connected = runCatching { repo.hasAllPermissions() }.getOrDefault(false)
            _state.update {
                it.copy(
                    snapshot = snap,
                    loading = false,
                    connected = connected && !snap.usingSampleData
                )
            }
        }
    }

    fun onPermissionResult(granted: Set<String>) {
        if (granted.containsAll(repo.permissions)) {
            refresh()
        } else {
            _state.update {
                it.copy(message = "Allow the health permissions to show live Galaxy Ring data.")
            }
        }
    }
}
