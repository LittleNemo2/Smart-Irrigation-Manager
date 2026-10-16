package com.austinbordeaux.irrigation.hardware;

import com.austinbordeaux.irrigation.model.Zone;
import org.springframework.stereotype.Component;

@Component
public class SimulatedValveController implements ValveController {

    public static final double MOISTURE_GAIN_PER_MINUTE = 0.6;
    public static final double MAX_ABOVE_TARGET = 3.0;

    @Override
    public double run(Zone zone, int minutes) {
        double raised = zone.getCurrentMoisture() + minutes * MOISTURE_GAIN_PER_MINUTE;
        zone.setCurrentMoisture(Math.min(raised, zone.getTargetMoisture() + MAX_ABOVE_TARGET));
        return zone.gallonsFor(minutes);
    }
}