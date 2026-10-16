package com.austinbordeaux.irrigation.engine;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Order(1)
public class SoilMoistureRule extends AbstractDecisionRule {

    public SoilMoistureRule() {
        super("Soil moisture");
    }

    @Override
    protected int adjustment(DecisionContext c) {
        double band = c.target() - c.threshold();
        double deficit = (c.target() - c.soilMoisture()) / band;
        return (int) Math.round(deficit * 50 - 25);
    }

    @Override
    protected String explain(DecisionContext c, int adjustment) {
        String text = String.format(Locale.US, "Soil is %.0f%% (threshold %.0f%%, target %.0f%%)",
                c.soilMoisture(), c.threshold(), c.target());
        if (c.soilMoisture() < c.threshold()) {
            return text + ": below threshold, needs water";
        }
        if (c.soilMoisture() >= c.target()) {
            return text + ": already at target";
        }
        return text + ": within range";
    }
}