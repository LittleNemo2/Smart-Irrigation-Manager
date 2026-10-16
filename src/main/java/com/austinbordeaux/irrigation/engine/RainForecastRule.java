package com.austinbordeaux.irrigation.engine;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Order(2)
public class RainForecastRule extends AbstractDecisionRule {

    public static final double SOAKING_RAIN_INCHES = 0.25;

    public RainForecastRule() {
        super("Rain forecast");
    }

    @Override
    protected int adjustment(DecisionContext c) {
        int chance = Math.max(c.today().rainChance(), c.tomorrow().rainChance());
        double rain = c.today().rainInches() + c.tomorrow().rainInches();
        int adjustment = -(int) Math.round(chance * 0.4);
        if (rain >= SOAKING_RAIN_INCHES) {
            adjustment -= 20;
        }
        return adjustment;
    }

    @Override
    protected String explain(DecisionContext c, int adjustment) {
        int chance = Math.max(c.today().rainChance(), c.tomorrow().rainChance());
        double rain = c.today().rainInches() + c.tomorrow().rainInches();
        String text = String.format(Locale.US, "%d%% chance of rain, %.2f in. expected in the next 48 hours",
                chance, rain);
        if (adjustment <= -30) {
            return text + ": rain should do the watering";
        }
        if (adjustment == 0) {
            return text + ": dry forecast";
        }
        return text;
    }
}