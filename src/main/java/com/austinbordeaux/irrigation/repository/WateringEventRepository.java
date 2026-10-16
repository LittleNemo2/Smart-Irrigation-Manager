package com.austinbordeaux.irrigation.repository;

import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.WateringEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WateringEventRepository extends JpaRepository<WateringEvent, Long> {

    List<WateringEvent> findTop10ByZone_PropertyOrderByRunAtDesc(Property property);
}