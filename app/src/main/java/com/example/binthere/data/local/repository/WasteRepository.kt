package com.example.binthere.data.repository

import com.example.binthere.data.local.dao.WasteDao
import com.example.binthere.data.local.entities.*
import com.example.binthere.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

data class FloodWeatherStatus(
    val temperature: String,
    val condition: String,
    val isFloodRiskHigh: Boolean,
    val advisoryMessage: String
)

class WasteRepository(private val wasteDao: WasteDao) {

    private val apiKey = "YOUR_OPENWEATHER_API_KEY"

    val allAlerts: Flow<List<CleanupAlertEntity>> = wasteDao.getAllAlerts()
    val allSchedules: Flow<List<PickupScheduleEntity>> = wasteDao.getAllSchedules()
    val allCollectionPoints: Flow<List<CollectionPointEntity>> = wasteDao.getAllCollectionPoints()

    suspend fun addAlert(alert: CleanupAlertEntity) = wasteDao.insertAlert(alert)

    suspend fun isDuplicateAlert(landmark: String, issueType: String): Boolean =
        wasteDao.countDuplicatePendingAlerts(landmark.trim(), issueType) > 0

    suspend fun updateAlert(alert: CleanupAlertEntity) = wasteDao.updateAlert(alert)

    suspend fun deleteAlert(alert: CleanupAlertEntity) = wasteDao.deleteAlert(alert)

    suspend fun addSchedule(schedule: PickupScheduleEntity) = wasteDao.insertSchedule(schedule)

    suspend fun deleteSchedule(schedule: PickupScheduleEntity) = wasteDao.deleteSchedule(schedule)

    suspend fun fetchNairobiFloodStatus(): FloodWeatherStatus {
        return try {
            if (apiKey == "YOUR_OPENWEATHER_API_KEY") {
                throw IllegalStateException("Using offline Nairobi seasonal forecast")
            }
            val response = RetrofitClient.weatherService.getNairobiWeather(apiKey = apiKey)
            val mainCondition = response.weather.firstOrNull()?.main ?: "Clear"
            val desc = response.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Clear skies"
            val tempStr = "${response.main.temp.toInt()}°C"
            val isRaining = mainCondition.contains("Rain", ignoreCase = true) ||
                    mainCondition.contains("Thunderstorm", ignoreCase = true) ||
                    (response.rain?.`1h` ?: 0.0) > 0.0

            if (isRaining) {
                FloodWeatherStatus(
                    temperature = tempStr,
                    condition = desc,
                    isFloodRiskHigh = true,
                    advisoryMessage = "HIGH FLOOD RISK: Rain detected in Nairobi. Prioritize clearing clogged drains immediately!"
                )
            } else {
                FloodWeatherStatus(
                    temperature = tempStr,
                    condition = desc,
                    isFloodRiskHigh = false,
                    advisoryMessage = "LOW FLOOD RISK: Dry conditions in Nairobi. Ideal window for scheduled waste pickups."
                )
            }
        } catch (e: Exception) {
            FloodWeatherStatus(
                temperature = "21°C",
                condition = "Scattered Showers (Seasonal Alert)",
                isFloodRiskHigh = true,
                advisoryMessage = "FLOOD WATCH (Nairobi Short Rains): Ensure CBD & estate storm drains are cleared before evening downpours."
            )
        }
    }
}