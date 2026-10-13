package com.austinbordeaux.irrigation.repository;

import com.austinbordeaux.irrigation.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}