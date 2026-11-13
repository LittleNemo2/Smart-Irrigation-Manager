package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.engine.RuleResult;

import java.time.LocalDateTime;
import java.util.List;

/** What the decision engine would do for a zone right now, with each rule's contribution. */
public record ZoneDecisionPreview(Long zoneId,
                                  String zone,
                                  String property,
                                  int probability,
                                  boolean willWater,
                                  String level,
                                  String summary,
                                  List<RuleResult> rules,
                                  boolean liveWeather,
                                  LocalDateTime generatedAt) {
}
