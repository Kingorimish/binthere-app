package com.example.binthere.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val name: String,
    val main: MainWeather,
    val weather: List<WeatherCondition>,
    val rain: RainVolume? = null
)

data class MainWeather(
    val temp: Double,
    val humidity: Int
)

data class WeatherCondition(
    val main: String,
    val description: String
)

data class RainVolume(
    val `1h`: Double? = 0.0
)

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getNairobiWeather(
        @Query("q") city: String = "Nairobi,KE",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val weatherService: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}