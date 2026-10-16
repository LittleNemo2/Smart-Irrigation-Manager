package com.austinbordeaux.irrigation.engine;

import java.util.List;

public record Decision(int probability, boolean willWater, List<RuleResult> results, String summary) {

    public static Decision notRunning(String reason) {
        return new Decision(0, false, List.of(), reason);
    }

    public String level() {
        if (probability >= 70) return "high";
        if (probability >= 40) return "medium";
        return "low";
    }
}