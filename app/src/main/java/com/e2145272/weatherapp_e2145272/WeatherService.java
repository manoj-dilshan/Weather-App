package com.e2145272.weatherapp_e2145272;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid, @Query("units") String units);

}
