package com.austinbordeaux.irrigation.config;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.Role;
import com.austinbordeaux.irrigation.model.Schedule;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.repository.AppUserRepository;
import com.austinbordeaux.irrigation.repository.PlantTypeRepository;
import com.austinbordeaux.irrigation.repository.PropertyRepository;
import com.austinbordeaux.irrigation.service.IrrigationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "Irrigate#2026";

    private static final String TURF = "Cool-season turf";
    private static final String GARDEN = "Garden / vegetables";
    private static final String SHRUBS = "Shrubs";
    private static final String TREES = "Trees";
    private static final String NATIVE = "Xeriscape / native";

    private static final Set<DayOfWeek> DAILY = EnumSet.allOf(DayOfWeek.class);
    private static final Set<DayOfWeek> MON_WED_FRI =
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    private static final Set<DayOfWeek> TUE_THU_SAT =
            EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY);

    private final PlantTypeRepository plantTypeRepository;
    private final AppUserRepository appUserRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;
    private final IrrigationService irrigationService;

    public DemoDataSeeder(PlantTypeRepository plantTypeRepository, AppUserRepository appUserRepository,
                          PropertyRepository propertyRepository, PasswordEncoder passwordEncoder,
                          IrrigationService irrigationService) {
        this.plantTypeRepository = plantTypeRepository;
        this.appUserRepository = appUserRepository;
        this.propertyRepository = propertyRepository;
        this.passwordEncoder = passwordEncoder;
        this.irrigationService = irrigationService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPlantTypes();
        seedUsers();
        seedProperties();
        irrigationService.backfillHistory(30);
    }

    private void seedPlantTypes() {
        plantType(TURF, 0.80);
        plantType(GARDEN, 0.90);
        plantType(SHRUBS, 0.50);
        plantType(TREES, 0.60);
        plantType(NATIVE, 0.30);
    }

    private void plantType(String name, double cropCoefficient) {
        if (!plantTypeRepository.existsByNameIgnoreCase(name)) {
            plantTypeRepository.save(new PlantType(name, cropCoefficient));
        }
    }

    private void seedUsers() {
        user("admin", "Site Administrator", "admin@example.com", Role.ADMIN);
        user("homeowner", "Hannah Homeowner", "hannah@example.com", Role.HOMEOWNER);
        user("landscaper", "Leo Landscaper", "leo@example.com", Role.LANDSCAPER);
    }

    private void user(String username, String fullName, String email, Role role) {
        if (!appUserRepository.existsByUsernameIgnoreCase(username)) {
            String hash = passwordEncoder.encode(DEMO_PASSWORD);
            appUserRepository.save(new AppUser(username, hash, fullName, email, role));
        }
    }

    private void seedProperties() {
        if (propertyRepository.count() > 0) {
            return;
        }
        Map<String, PlantType> types = new HashMap<>();
        for (PlantType type : plantTypeRepository.findAllByOrderByNameAsc()) {
            types.put(type.getName(), type);
        }
        AppUser hannah = appUserRepository.findByUsernameIgnoreCase("homeowner").orElseThrow();
        AppUser leo = appUserRepository.findByUsernameIgnoreCase("landscaper").orElseThrow();

        Property maple = new Property("Maple Street Home",
                "1420 Maple St, Mountain Home, ID 83647", 43.1330, -115.6912, hannah);
        zone(maple, "Front Lawn", types.get(TURF), 8.0, 20, 32, true, LocalTime.of(5, 30), DAILY, 20).setCurrentMoisture(18);
        zone(maple, "Back Lawn", types.get(TURF), 9.5, 20, 32, true, LocalTime.of(5, 50), DAILY, 20).setCurrentMoisture(27);
        zone(maple, "Garden Beds (drip)", types.get(GARDEN), 2.0, 22, 34, true, LocalTime.of(6, 10), DAILY, 20).setCurrentMoisture(33);

        Property cabin = new Property("Sagebrush Cabin",
                "88 Juniper Ln, Mountain Home, ID 83647", 43.1452, -115.7034, hannah);
        zone(cabin, "Native Beds", types.get(NATIVE), 3.0, 10, 20, true, LocalTime.of(6, 0), MON_WED_FRI, 15).setCurrentMoisture(12);
        zone(cabin, "Windbreak Trees", types.get(TREES), 5.0, 16, 30, false, LocalTime.of(6, 30), TUE_THU_SAT, 40);

        Property hoa = new Property("Canyon Ridge HOA Commons",
                "300 Canyon Ridge Dr, Mountain Home, ID 83647", 43.1205, -115.6788, leo);
        zone(hoa, "Entrance Turf", types.get(TURF), 14.0, 20, 32, true, LocalTime.of(4, 0), DAILY, 20).setCurrentMoisture(22);
        zone(hoa, "Park Shrubs", types.get(SHRUBS), 6.0, 16, 28, false, LocalTime.of(4, 30), MON_WED_FRI, 25);

        Property office = new Property("Sunset Office Park",
                "2200 E 8th N, Mountain Home, ID 83647", 43.1398, -115.6655, leo);
        zone(office, "Courtyard Trees", types.get(TREES), 5.0, 16, 30, true, LocalTime.of(3, 30), TUE_THU_SAT, 40).setCurrentMoisture(20);
        zone(office, "Parking Strip Natives", types.get(NATIVE), 4.0, 10, 20, true, LocalTime.of(3, 45), MON_WED_FRI, 15).setCurrentMoisture(19);

        propertyRepository.saveAll(List.of(maple, cabin, hoa, office));
    }

    private Zone zone(Property property, String name, PlantType type, double gpm,
                      double threshold, double target, boolean online,
                      LocalTime start, Set<DayOfWeek> days, int minutes) {
        Zone zone = new Zone(name, type, gpm, threshold, target);
        zone.setOnline(online);
        property.addZone(zone);
        zone.addSchedule(new Schedule(start, days, minutes));
        return zone;
    }
}