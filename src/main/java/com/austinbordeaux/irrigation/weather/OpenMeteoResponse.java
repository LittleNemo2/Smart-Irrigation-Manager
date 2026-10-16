package com.austinbordeaux.irrigation.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Current current, Daily daily) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(Double temperature_2m,
                          Double relative_humidity_2m,
                          Double wind_speed_10m,
                          Double weather_code) {
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(List<String> time,
                        List<Double> temperature_2m_max,
                        List<Double> temperature_2m_min,
                        List<Double> precipitation_sum,
                        List<Double> precipitation_probability_max,
                        List<Double> et0_fao_evapotranspiration,
                        List<Double> weather_code) {
    }
}