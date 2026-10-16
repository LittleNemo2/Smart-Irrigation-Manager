package com.austinbordeaux.irrigation.weather;

public interface WeatherClient {
    WeatherReport fetch(double latitude, double longitude);
}