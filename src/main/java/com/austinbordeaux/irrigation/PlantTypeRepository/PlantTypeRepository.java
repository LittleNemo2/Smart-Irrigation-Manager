package com.austinbordeaux.irrigation.PlantTypeRepository;

import com.austinbordeaux.irrigation.model.PlantType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantTypeRepository extends JpaRepository<PlantType, Long> {

    List<PlantType> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}