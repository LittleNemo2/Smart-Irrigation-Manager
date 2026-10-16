package com.austinbordeaux.irrigation.weather;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

    private static final String URL = "https://api.open-meteo.com/v1/forecast"
            + "?latitude={lat}&longitude={lon}"
            + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
            + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,"
            + "precipitation_probability_max,et0_fao_evapotranspiration,weather_code"
            + "&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch"
            + "&timezone=auto&forecast_days=7";

    private final RestClient restClient;

    public OpenMeteoWeatherClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public WeatherReport fetch(double latitude, double longitude) {
        OpenMeteoResponse response = restClient.get()
                .uri(URL, latitude, longitude)
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null || response.current() == null || response.daily() == null
                || response.daily().time() == null) {
            throw new IllegalStateException("Open-Meteo returned an empty response");
        }

        OpenMeteoResponse.Daily daily = response.daily();
        List<DailyForecast> days = new ArrayList<>();
        for (int i = 0; i < daily.time().size(); i++) {
            days.add(new DailyForecast(
                    LocalDate.parse(daily.time().get(i)),
                    value(daily.temperature_2m_max(), i),
                    value(daily.temperature_2m_min(), i),
                    value(daily.precipitation_sum(), i),
                    (int) Math.round(value(daily.precipitation_probability_max(), i)),
                    value(daily.et0_fao_evapotranspiration(), i),
                    (int) value(daily.weather_code(), i)));
        }

        OpenMeteoResponse.Current now = response.current();
        return new WeatherReport(
                orZero(now.temperature_2m()),
                (int) Math.round(orZero(now.relative_humidity_2m())),
                orZero(now.wind_speed_10m()),
                (int) orZero(now.weather_code()),
                days,
                LocalDateTime.now(),
                true);
    }

    private static double value(List<Double> list, int index) {
        if (list == null || index >= list.size()) {
            return 0.0;
        }
        return orZero(list.get(index));
    }

    private static double orZero(Double number) {
        return number == null ? 0.0 : number;
    }
}