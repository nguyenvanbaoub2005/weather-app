package com.example.textn.data.api
import com.example.textn.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface WeatherApiService {
    @GET("data/3.0/onecall")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely,alerts",//
        @Query("lang") lang: String = "vi"
    ): Response<WeatherResponse>




//    companion object {
//        private const val BASE_URL = "https://api.openweathermap.org/"
//
//        fun create(apiKey: String): WeatherApiService {
//            val retrofit = Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//            return retrofit.create(WeatherApiService::class.java)
//        }
//    }
}
