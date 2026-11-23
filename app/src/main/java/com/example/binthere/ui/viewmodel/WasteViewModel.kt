package com.example.binthere.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binthere.data.local.entities.CleanupAlertEntity
import com.example.binthere.data.local.entities.PickupScheduleEntity
import com.example.binthere.data.repository.FloodWeatherStatus
import com.example.binthere.data.repository.WasteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WasteViewModel(private val repository: WasteRepository) : ViewModel() {

    val alerts = repository.allAlerts
    val schedules = repository.allSchedules
    val collectionPoints = repository.allCollectionPoints

    // Flow for Toast messages
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    private val _weatherStatus = MutableStateFlow(
        FloodWeatherStatus(
            temperature = "Loading...",
            condition = "Fetching weather...",
            isFloodRiskHigh = false,
            advisoryMessage = "Checking Nairobi forecast..."
        )
    )
    val weatherStatus: StateFlow<FloodWeatherStatus> = _weatherStatus.asStateFlow()

    init {
        refreshWeather()
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _weatherStatus.value = repository.fetchNairobiFloodStatus()
        }
    }

    fun submitCleanupAlert(zone: String, landmark: String, issueType: String, severity: String, reportedByRole: String) {
        viewModelScope.launch {
            if (zone.isBlank() || landmark.isBlank()) {
                _uiMessage.emit("Please fill in both Zone and Landmark.")
                return@launch
            }

            val isDuplicate = repository.isDuplicateAlert(landmark, issueType)
            if (isDuplicate) {
                _uiMessage.emit("An alert for this issue at this landmark is already pending.")
                return@launch
            }

            val newAlert = CleanupAlertEntity(
                zone = zone,
                landmark = landmark,
                issueType = issueType,
                severity = severity,
                reportedByRole = reportedByRole
            )
            repository.addAlert(newAlert)
            _uiMessage.emit("Alert reported successfully!")
        }
    }

    fun toggleAlertStatus(alert: CleanupAlertEntity) {
        viewModelScope.launch {
            val newStatus = if (alert.status == "Pending") "Cleared" else "Pending"
            repository.updateAlert(alert.copy(status = newStatus))
        }
    }

    fun deleteAlert(alert: CleanupAlertEntity) {
        viewModelScope.launch {
            repository.deleteAlert(alert)
            _uiMessage.emit("Alert deleted.")
        }
    }

    // NEW FUNCTION: Handles adding the schedule and returning it for the notification callback
    fun submitPickupSchedule(
        zone: String,
        collectionPoint: String,
        pickupDate: String,
        pickupTime: String,
        organizerRole: String,
        notes: String,
        onScheduleCreated: (PickupScheduleEntity) -> Unit
    ) {
        viewModelScope.launch {
            if (zone.isBlank() || collectionPoint.isBlank()) {
                _uiMessage.emit("Zone and Collection Point are required.")
                return@launch
            }

            val newSchedule = PickupScheduleEntity(
                zone = zone,
                collectionPoint = collectionPoint,
                pickupDate = pickupDate,
                pickupTime = pickupTime,
                organizerRole = organizerRole,
                notes = notes
            )

            repository.addSchedule(newSchedule)
            _uiMessage.emit("Pickup scheduled successfully!")

            // Trigger the callback so the Fragment can start the Worker
            onScheduleCreated(newSchedule)
        }
    }

    fun deleteSchedule(schedule: PickupScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            _uiMessage.emit("Schedule deleted.")
        }
    }
}