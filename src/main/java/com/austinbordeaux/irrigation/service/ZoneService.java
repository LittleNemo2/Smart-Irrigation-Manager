package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.repository.PlantTypeRepository;
import com.austinbordeaux.irrigation.repository.ZoneRepository;
import com.austinbordeaux.irrigation.web.form.ZoneForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ZoneService {

    private final PropertyService propertyService;
    private final ZoneRepository zoneRepository;
    private final PlantTypeRepository plantTypeRepository;

    public ZoneService(PropertyService propertyService, ZoneRepository zoneRepository,
                       PlantTypeRepository plantTypeRepository) {
        this.propertyService = propertyService;
        this.zoneRepository = zoneRepository;
        this.plantTypeRepository = plantTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<PlantType> plantTypes() {
        return plantTypeRepository.findAllByOrderByNameAsc();
    }

    public Zone get(Long propertyId, Long zoneId, AppUser user) {
        Property property = propertyService.get(propertyId, user);
        return property.getZones().stream()
                .filter(z -> z.getId().equals(zoneId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Zone", zoneId));
    }

    public Zone create(Long propertyId, ZoneForm form, AppUser user) {
        Property property = propertyService.get(propertyId, user);
        Zone zone = new Zone(form.getName(), plantType(form.getPlantTypeId()),
                form.getFlowRateGpm(), form.getMoistureThreshold(), form.getTargetMoisture());
        zone.setOnline(form.isOnline());
        property.addZone(zone);
        return zoneRepository.save(zone);
    }

    public Zone update(Long propertyId, Long zoneId, ZoneForm form, AppUser user) {
        Zone zone = get(propertyId, zoneId, user);
        zone.setName(form.getName());
        zone.setPlantType(plantType(form.getPlantTypeId()));
        zone.setFlowRateGpm(form.getFlowRateGpm());
        zone.setMoistureThreshold(form.getMoistureThreshold());
        zone.setTargetMoisture(form.getTargetMoisture());
        zone.setOnline(form.isOnline());
        return zone;
    }

    public void delete(Long propertyId, Long zoneId, AppUser user) {
        Property property = propertyService.get(propertyId, user);
        Zone zone = get(propertyId, zoneId, user);
        property.removeZone(zone);
    }

    private PlantType plantType(Long id) {
        return plantTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plant type", id));
    }
}