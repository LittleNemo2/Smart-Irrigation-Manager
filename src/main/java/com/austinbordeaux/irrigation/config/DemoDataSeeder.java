package com.austinbordeaux.irrigation.config;

import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.repository.PlantTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final PlantTypeRepository plantTypes;

    public DemoDataSeeder(PlantTypeRepository plantTypes) {
        this.plantTypes = plantTypes;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (plantTypes.count() > 0) {
            return;
        }
        plantTypes.saveAll(List.of(
                new PlantType("Cool-season turf", 0.80),
                new PlantType("Garden / vegetables", 0.90),
                new PlantType("Shrubs", 0.50),
                new PlantType("Trees", 0.60),
                new PlantType("Xeriscape / native", 0.30)
        ));
        log.info("Loaded {} plant types", plantTypes.count());
    }
}