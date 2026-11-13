package com.austinbordeaux.irrigation.repository;

import com.austinbordeaux.irrigation.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    long countByOnlineTrue();
}