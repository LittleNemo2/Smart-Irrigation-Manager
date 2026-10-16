package com.austinbordeaux.irrigation.engine;

import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.weather.DailyForecast;
import com.austinbordeaux.irrigation.weather.WeatherReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IrrigationDecisionEngine {

    public static final int BASE_PROBABILITY = 50;
    public static final int WATER_AT = 50;

    private final List<DecisionRule> rules;

    public IrrigationDecisionEngine(List<DecisionRule> rules) {
        this.rules = rules;
    }

    public Decision decide(Zone zone, WeatherReport weather) {
        if (!zone.isOnline()) {
            return Decision.notRunning("Controller offline: zone can't run");
        }
        if (!zone.hasActiveSchedule()) {
            return Decision.notRunning("No active schedule");
        }

        List<DailyForecast> days = weather.days();
        DailyForecast today = days.get(0);
        DailyForecast tomorrow = days.size() > 1 ? days.get(1) : today;
        DecisionContext context = new DecisionContext(zone, zone.getCurrentMoisture(), today, tomorrow);

        List<RuleResult> results = new ArrayList<>();
        int probability = BASE_PROBABILITY;
        for (DecisionRule rule : rules) {
            RuleResult result = rule.evaluate(context);
            results.add(result);
            probability += result.adjustment();
        }
        probability = Math.max(0, Math.min(100, probability));

        boolean willWater = probability >= WATER_AT;
        String summary = willWater ? "Will water at next scheduled time" : "Will skip next watering";
        return new Decision(probability, willWater, results, summary);
    }
}