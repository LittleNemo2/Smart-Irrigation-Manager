package com.austinbordeaux.irrigation.PlantTypeRepository;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByOwnerOrderByNameAsc(AppUser owner);

    List<Property> findAllByOrderByNameAsc();
}