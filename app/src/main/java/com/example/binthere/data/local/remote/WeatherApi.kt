package com.example.binthere.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getNairobiWeather(
        @Query("q") city: String = "Nairobi",
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"

        fun create(): WeatherApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(WeatherApi::class.java)
        }
    }
}

// Data classes to match the OpenWeatherMap JSON response
data class WeatherResponse(
    val weather: List<WeatherDesc>,
    val main: MainTemp,
    val rain: RainVolume? = null
)
data class WeatherDesc(val main: String, val description: String)
data class MainTemp(val temp: Double)
data class RainVolume(val `1h`: Double? = null)