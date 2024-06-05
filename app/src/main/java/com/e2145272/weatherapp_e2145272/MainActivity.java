package com.e2145272.weatherapp_e2145272;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView latitudeLongitudeTextView;
    private TextView geoAddressTextView;
    private TextView systemTimeTextView;
    private TextView systemDateTextView;
    private TextView weatherMainTextView;
    private TextView weatherDescriptionTextView;
    private TextView temperatureTextView;
    private TextView feelsLikeTextView;
    private TextView tempMinTextView;
    private TextView tempMaxTextView;
    private TextView pressureTextView;
    private TextView humidityTextView;
    private TextView visibilityTextView;
    private TextView windSpeedTextView;
    private TextView rainTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        latitudeLongitudeTextView = findViewById(R.id.latitude_longitude);
        geoAddressTextView = findViewById(R.id.geo_address);
        systemTimeTextView = findViewById(R.id.system_time);
        systemDateTextView = findViewById(R.id.system_date);

        weatherMainTextView = findViewById(R.id.weather_main);
        weatherDescriptionTextView = findViewById(R.id.weather_description);
        temperatureTextView = findViewById(R.id.temperature);
        feelsLikeTextView = findViewById(R.id.feels_like);
        tempMinTextView = findViewById(R.id.temp_min);
        tempMaxTextView = findViewById(R.id.temp_max);
        pressureTextView = findViewById(R.id.pressure);
        humidityTextView = findViewById(R.id.humidity);
        visibilityTextView = findViewById(R.id.visibility);
        windSpeedTextView = findViewById(R.id.wind_speed);
        rainTextView = findViewById(R.id.rain);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getLastLocation();
        }

        displayCurrentTimeAndDate();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            latitudeLongitudeTextView.setText(String.format(Locale.getDefault(), "Latitude: %.4f and Longitude: %.4f", latitude, longitude));
                            getAddressFromLocation(latitude, longitude);
                            fetchWeatherData(latitude, longitude);
                        }
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                geoAddressTextView.setText(address.getAddressLine(0));
            } else {
                geoAddressTextView.setText("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            geoAddressTextView.setText("Geocoder service not available");
        }
    }

    private void fetchWeatherData(double latitude, double longitude) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(latitude, longitude, "a1396dcc665bfe00a8d2c8c72cd0fb48", "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    if (weatherResponse.weather != null && !weatherResponse.weather.isEmpty()) {
                        WeatherResponse.Weather weather = weatherResponse.weather.get(0);
                        weatherMainTextView.setText(weather.main);
                        weatherDescriptionTextView.setText(weather.description);
                    }

                    weatherMainTextView.setText(weatherResponse.weather.get(0).main);
                    weatherDescriptionTextView.setText(weatherResponse.weather.get(0).description);
                    temperatureTextView.setText(String.format(Locale.getDefault(), "%.1f°C", weatherResponse.main.temp));
                    feelsLikeTextView.setText(String.format(Locale.getDefault(), "%.2f°C", weatherResponse.main.feels_like));
                    tempMinTextView.setText(String.format(Locale.getDefault(), "%.0f°C", weatherResponse.main.temp_min));
                    tempMaxTextView.setText(String.format(Locale.getDefault(), "%.0f°C", weatherResponse.main.temp_max));
                    pressureTextView.setText(String.format(Locale.getDefault(), "%d hPa", weatherResponse.main.pressure));
                    humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", weatherResponse.main.humidity));
                    visibilityTextView.setText(String.format(Locale.getDefault(), "%d m", weatherResponse.visibility));
                    windSpeedTextView.setText(String.format(Locale.getDefault(), "%.2f m/s", weatherResponse.wind.speed));
                    rainTextView.setText(weatherResponse.rain != null ? String.format(Locale.getDefault(), "%.2f mm", weatherResponse.rain._1h) : "N/A");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayCurrentTimeAndDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        systemTimeTextView.setText(currentTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMMM d", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        systemDateTextView.setText(currentDate);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
}
