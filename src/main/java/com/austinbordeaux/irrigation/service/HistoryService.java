package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import com.austinbordeaux.irrigation.repository.WateringEventRepository;
import com.austinbordeaux.irrigation.web.form.HistoryFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HistoryService {

    public static final int MAX_RESULTS = 500;

    private final WateringEventRepository eventRepository;

    public HistoryService(WateringEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public HistoryResult search(AppUser user, HistoryFilter filter) {
        List<WateringEvent> all = eventRepository.search(
                user, user.isAdmin(), filter.getPropertyId(), filter.getZone(), filter.getOutcome(),
                filter.getFrom().atStartOfDay(), filter.getTo().plusDays(1).atStartOfDay());

        long watered = all.stream().filter(e -> e.getOutcome() == WateringOutcome.WATERED).count();
        double used = all.stream().mapToDouble(WateringEvent::getGallonsUsed).sum();
        double saved = all.stream().mapToDouble(WateringEvent::getGallonsSaved).sum();
        boolean truncated = all.size() > MAX_RESULTS;
        List<WateringEvent> shown = truncated ? all.subList(0, MAX_RESULTS) : all;

        return new HistoryResult(shown, all.size(), watered, all.size() - watered, used, saved, truncated);
    }

    public record HistoryResult(List<WateringEvent> events, int total, long watered, long skipped,
                                double gallonsUsed, double gallonsSaved, boolean truncated) {
    }
}