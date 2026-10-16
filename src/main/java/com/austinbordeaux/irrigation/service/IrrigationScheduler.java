package com.austinbordeaux.irrigation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class IrrigationScheduler {

    private static final Logger log = LoggerFactory.getLogger(IrrigationScheduler.class);

    private final IrrigationService irrigationService;

    public IrrigationScheduler(IrrigationService irrigationService) {
        this.irrigationService = irrigationService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void runDueSchedules() {
        int ran = irrigationService.runDueSchedules(LocalDateTime.now().withSecond(0).withNano(0));
        if (ran > 0) {
            log.info("Scheduler checked {} zone(s)", ran);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void hourlyDrying() {
        irrigationService.applyHourlyDrying();
    }
}