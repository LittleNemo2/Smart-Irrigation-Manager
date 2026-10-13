package com.austinbordeaux.irrigation.PlantTypeRepository;

import com.austinbordeaux.irrigation.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
}