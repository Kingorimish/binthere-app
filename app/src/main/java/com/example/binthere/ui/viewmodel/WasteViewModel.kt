package com.example.binthere.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.binthere.data.local.AppDatabase
import com.example.binthere.data.local.entities.*
import com.example.binthere.data.repository.FloodWeatherStatus
import com.example.binthere.data.repository.WasteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WasteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WasteRepository

    val alerts: StateFlow<List<CleanupAlertEntity>>
    val schedules: StateFlow<List<PickupScheduleEntity>>
    val collectionPoints: StateFlow<List<CollectionPointEntity>>

    private val _weatherStatus = MutableStateFlow(
        FloodWeatherStatus("21°C", "Loading Nairobi weather...", false, "Checking flood risk...")
    )
    val weatherStatus: StateFlow<FloodWeatherStatus> = _weatherStatus.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        val dao = AppDatabase.getDatabase(application).wasteDao()
        repository = WasteRepository(dao)

        alerts = repository.allAlerts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schedules = repository.allSchedules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        collectionPoints = repository.allCollectionPoints.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        refreshWeather()
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _weatherStatus.value = repository.fetchNairobiFloodStatus()
        }
    }

    fun submitCleanupAlert(
        zone: String,
        landmark: String,
        issueType: String,
        severity: String,
        reportedByRole: String
    ) {
        viewModelScope.launch {
            val cleanZone = zone.trim()
            val cleanLandmark = landmark.trim()

            if (cleanZone.isEmpty() || cleanLandmark.isEmpty()) {
                _uiMessage.emit("Please enter both a Nairobi zone and a specific landmark.")
                return@launch
            }
            if (cleanLandmark.length < 4) {
                _uiMessage.emit("Landmark is too short. Provide a clearer location (e.g., Moi Avenue).")
                return@launch
            }
            if (repository.isDuplicateAlert(cleanLandmark, issueType)) {
                _uiMessage.emit("A pending '$issueType' alert already exists at $cleanLandmark!")
                return@launch
            }

            repository.addAlert(
                CleanupAlertEntity(
                    zone = cleanZone,
                    landmark = cleanLandmark,
                    issueType = issueType,
                    severity = severity,
                    reportedByRole = reportedByRole
                )
            )
            _uiMessage.emit("Cleanup alert logged for $cleanLandmark!")
        }
    }

    fun toggleAlertStatus(alert: CleanupAlertEntity) {
        viewModelScope.launch {
            val newStatus = if (alert.status == "Pending") "Cleared" else "Pending"
            repository.updateAlert(alert.copy(status = newStatus))
            _uiMessage.emit("Alert marked as $newStatus.")
        }
    }

    fun deleteAlert(alert: CleanupAlertEntity) {
        viewModelScope.launch {
            repository.deleteAlert(alert)
            _uiMessage.emit("Alert deleted.")
        }
    }

    fun submitPickupSchedule(
        zone: String,
        collectionPoint: String,
        pickupDate: String,
        pickupTime: String,
        organizerRole: String,
        notes: String,
        onScheduledSuccess: (PickupScheduleEntity) -> Unit
    ) {
        viewModelScope.launch {
            if (zone.isBlank() || collectionPoint.isBlank() || pickupDate.isBlank() || pickupTime.isBlank()) {
                _uiMessage.emit("Please fill in zone, collection point, date, and time.")
                return@launch
            }

            val newSchedule = PickupScheduleEntity(
                zone = zone.trim(),
                collectionPoint = collectionPoint.trim(),
                pickupDate = pickupDate.trim(),
                pickupTime = pickupTime.trim(),
                organizerRole = organizerRole,
                notes = notes.trim().ifEmpty { "Bring gloves and heavy-duty bags." }
            )
            repository.addSchedule(newSchedule)
            _uiMessage.emit("Pickup scheduled for ${newSchedule.zone} on ${newSchedule.pickupDate}!")
            onScheduledSuccess(newSchedule)
        }
    }

    fun deleteSchedule(schedule: PickupScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            _uiMessage.emit("Pickup schedule removed.")
        }
    }
}