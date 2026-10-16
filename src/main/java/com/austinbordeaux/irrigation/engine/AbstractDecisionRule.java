package com.austinbordeaux.irrigation.engine;

public abstract class AbstractDecisionRule implements DecisionRule {

    public static final int MAX_ADJUSTMENT = 50;

    private final String name;

    protected AbstractDecisionRule(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public final RuleResult evaluate(DecisionContext context) {
        int adjustment = Math.max(-MAX_ADJUSTMENT, Math.min(MAX_ADJUSTMENT, adjustment(context)));
        return new RuleResult(name, adjustment, explain(context, adjustment));
    }

    protected abstract int adjustment(DecisionContext context);

    protected abstract String explain(DecisionContext context, int adjustment);
}