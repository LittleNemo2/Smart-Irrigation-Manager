package com.austinbordeaux.irrigation.engine;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Order(3)
public class EvapotranspirationRule extends AbstractDecisionRule {

    public static final double NEUTRAL_INCHES = 0.10;

    public EvapotranspirationRule() {
        super("Evapotranspiration");
    }

    @Override
    protected int adjustment(DecisionContext c) {
        double cropEt = c.today().et0Inches() * c.cropCoefficient();
        return (int) Math.round((cropEt - NEUTRAL_INCHES) * 200);
    }

    @Override
    protected String explain(DecisionContext c, int adjustment) {
        double cropEt = c.today().et0Inches() * c.cropCoefficient();
        String text = String.format(Locale.US, "Plants will lose about %.2f in. today (ET₀ %.2f × crop factor %.2f)",
                cropEt, c.today().et0Inches(), c.cropCoefficient());
        if (adjustment >= 10) {
            return text + ": hot, dry day";
        }
        if (adjustment <= -10) {
            return text + ": low water use";
        }
        return text;
    }
}