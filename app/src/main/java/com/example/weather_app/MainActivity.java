package com.example.weather_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView cityNameText, temperatureText, humidityText, descriptionText, windText;
    private ImageView weatherIcon;
    private Button refreshButton;
    private EditText cityNameInput;

    private static final String API_KEY = "ce8fee4c16026694fb9838b662c19e9a";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.Temperature);
        humidityText = findViewById(R.id.HumidityText);
        windText = findViewById(R.id.WindText);
        descriptionText = findViewById(R.id.descriptionText);
        refreshButton = findViewById(R.id.fetchWeatherButton);
        cityNameInput = findViewById(R.id.cityNameInput);

        // Set button click listener
        refreshButton.setOnClickListener(v -> fetchWeather(cityNameInput.getText().toString().trim()));
    }

    private void fetchWeather(String city) {
        if (city.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build URL
        String url = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    Log.e("WeatherApp", "API call failed", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) { // Properly close the response
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> parseAndDisplayData(responseData));
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "City not found. Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                            Log.e("WeatherApp", "Response error: " + response.message());
                        });
                    }
                }
            }
        });
    }

    private void parseAndDisplayData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // Extract city name
            String cityName = jsonObject.optString("name", "N/A");

            // Extract main data
            JSONObject main = jsonObject.optJSONObject("main");
            String temperature = main != null ? main.optString("temp", "N/A") + "°C" : "N/A";
            String humidity = main != null ? main.optString("humidity", "N/A") + "%" : "N/A";

            // Extract weather description
            String description = jsonObject.optJSONArray("weather") != null
                    ? jsonObject.optJSONArray("weather").optJSONObject(0).optString("description", "N/A")
                    : "N/A";

            // Extract wind speed
            JSONObject wind = jsonObject.optJSONObject("wind");
            String windSpeed = wind != null ? wind.optString("speed", "N/A") + " km/h" : "N/A";

            // Update UI
            cityNameText.setText(cityName);
            temperatureText.setText(temperature);
            humidityText.setText(humidity);
            descriptionText.setText(description);
            windText.setText(windSpeed);
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "JSON parsing error", e);
        }
    }
}
