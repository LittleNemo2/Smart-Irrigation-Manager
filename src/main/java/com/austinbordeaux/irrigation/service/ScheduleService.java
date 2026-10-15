package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Schedule;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.repository.ScheduleRepository;
import com.austinbordeaux.irrigation.web.form.ScheduleForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleService {

    private final ZoneService zoneService;
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ZoneService zoneService, ScheduleRepository scheduleRepository) {
        this.zoneService = zoneService;
        this.scheduleRepository = scheduleRepository;
    }

    public Schedule get(Long propertyId, Long zoneId, Long scheduleId, AppUser user) {
        Zone zone = zoneService.get(propertyId, zoneId, user);
        return zone.getSchedules().stream()
                .filter(s -> s.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Schedule", scheduleId));
    }

    public Schedule create(Long propertyId, Long zoneId, ScheduleForm form, AppUser user) {
        Zone zone = zoneService.get(propertyId, zoneId, user);
        Schedule schedule = new Schedule(form.getStartTime(), form.getDays(), form.getDurationMinutes());
        schedule.setEnabled(form.isEnabled());
        zone.addSchedule(schedule);
        return scheduleRepository.save(schedule);
    }

    public Schedule update(Long propertyId, Long zoneId, Long scheduleId, ScheduleForm form, AppUser user) {
        Schedule schedule = get(propertyId, zoneId, scheduleId, user);
        schedule.setStartTime(form.getStartTime());
        schedule.setDays(form.getDays());
        schedule.setDurationMinutes(form.getDurationMinutes());
        schedule.setEnabled(form.isEnabled());
        return schedule;
    }

    public void delete(Long propertyId, Long zoneId, Long scheduleId, AppUser user) {
        Zone zone = zoneService.get(propertyId, zoneId, user);
        Schedule schedule = get(propertyId, zoneId, scheduleId, user);
        zone.removeSchedule(schedule);
    }
}