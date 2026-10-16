package com.austinbordeaux.irrigation.hardware;

import com.austinbordeaux.irrigation.model.Zone;

public interface ValveController {

    double run(Zone zone, int minutes);
}