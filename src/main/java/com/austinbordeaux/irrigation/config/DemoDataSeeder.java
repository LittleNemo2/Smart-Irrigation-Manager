package com.austinbordeaux.irrigation.config;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.model.Role;
import com.austinbordeaux.irrigation.repository.AppUserRepository;
import com.austinbordeaux.irrigation.repository.PlantTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "Irrigate#2026";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final PlantTypeRepository plantTypes;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(PlantTypeRepository plantTypes, AppUserRepository users,
                          PasswordEncoder passwordEncoder) {
        this.plantTypes = plantTypes;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPlantTypes();
        seedUsers();
    }

    private void seedPlantTypes() {
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

    private void seedUsers() {
        if (users.count() > 0) {
            return;
        }
        users.saveAll(List.of(
                new AppUser("admin", passwordEncoder.encode(DEMO_PASSWORD),
                        "Site Administrator", "admin@example.com", Role.ADMIN),
                new AppUser("homeowner", passwordEncoder.encode(DEMO_PASSWORD),
                        "Hannah Homeowner", "hannah@example.com", Role.HOMEOWNER),
                new AppUser("landscaper", passwordEncoder.encode(DEMO_PASSWORD),
                        "Leo Landscaper", "leo@bordeauxlandscaping.example", Role.LANDSCAPER)
        ));
        log.info("Loaded demo users (password: {})", DEMO_PASSWORD);
    }
}