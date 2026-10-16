package com.austinbordeaux.irrigation.engine;

public interface DecisionRule {

    String name();

    RuleResult evaluate(DecisionContext context);
}