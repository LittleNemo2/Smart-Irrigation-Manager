package com.austinbordeaux.irrigation;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import com.austinbordeaux.irrigation.report.UsageReport;
import com.austinbordeaux.irrigation.repository.AppUserRepository;
import com.austinbordeaux.irrigation.repository.PropertyRepository;
import com.austinbordeaux.irrigation.repository.WateringEventRepository;
import com.austinbordeaux.irrigation.service.HistoryService;
import com.austinbordeaux.irrigation.service.HistoryService.HistoryResult;
import com.austinbordeaux.irrigation.service.NotFoundException;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.service.ReportService;
import com.austinbordeaux.irrigation.web.form.HistoryFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the real application against PostgreSQL in Docker, the same database used in production,
 * so the JPQL search queries and schema are verified on PostgreSQL rather than only on H2.
 * Skipped automatically when Docker is not available.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private AppUserRepository users;

    @Autowired
    private PropertyRepository properties;

    @Autowired
    private WateringEventRepository events;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ReportService reportService;

    private AppUser user(String username) {
        return users.findByUsernameIgnoreCase(username).orElseThrow();
    }

    @Test
    @DisplayName("Demo users, properties, and 30 days of history are seeded into PostgreSQL")
    void seedsDemoData() {
        assertThat(users.count()).isEqualTo(3);
        assertThat(properties.count()).isEqualTo(4);
        assertThat(events.count()).isPositive();
    }

    @Test
    @DisplayName("History search with no optional filters runs on PostgreSQL")
    void historySearchWithoutFilters() {
        HistoryResult result = historyService.search(user("admin"), new HistoryFilter());

        assertThat(result.total()).isPositive();
        assertThat(result.watered() + result.skipped()).isEqualTo(result.total());
    }

    @Test
    @DisplayName("History search applies the zone-name and outcome filters")
    void historySearchWithFilters() {
        HistoryFilter byZone = new HistoryFilter();
        byZone.setZone("LAWN");
        HistoryResult lawns = historyService.search(user("admin"), byZone);
        assertThat(lawns.events()).isNotEmpty()
                .allSatisfy(e -> assertThat(e.getZone().getName().toLowerCase(Locale.ROOT)).contains("lawn"));

        HistoryFilter byOutcome = new HistoryFilter();
        byOutcome.setOutcome(WateringOutcome.SKIPPED);
        HistoryResult skipped = historyService.search(user("admin"), byOutcome);
        assertThat(skipped.events()).allSatisfy(e -> assertThat(e.getOutcome()).isEqualTo(WateringOutcome.SKIPPED));
    }

    @Test
    @DisplayName("Non-admin users only see watering history for properties they own")
    void historyIsScopedToOwner() {
        AppUser hannah = user("homeowner");
        HistoryResult result = historyService.search(hannah, new HistoryFilter());

        assertThat(result.events()).isNotEmpty()
                .allSatisfy(e -> assertThat(e.getZone().getProperty().isOwnedBy(hannah)).isTrue());
    }

    @Test
    @DisplayName("A user cannot open another user's property")
    void ownershipCheckHidesOtherUsersProperties() {
        AppUser hannah = user("homeowner");
        AppUser leo = user("landscaper");
        Property leosProperty = properties.findAll().stream()
                .filter(p -> p.isOwnedBy(leo))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> propertyService.get(leosProperty.getId(), hannah))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("The usage report's totals match the watering history for the same date range")
    void usageReportMatchesHistory() {
        AppUser admin = user("admin");
        HistoryFilter filter = new HistoryFilter();
        HistoryResult history = historyService.search(admin, filter);
        UsageReport report = reportService.usageReport(admin, filter.getFrom(), filter.getTo());

        assertThat(report.getTotals().runs()).isEqualTo(history.total());
        assertThat(report.getTotals().gallonsUsed()).isCloseTo(history.gallonsUsed(),
                org.assertj.core.data.Offset.offset(0.01));
        double eventTotal = history.events().stream().mapToDouble(WateringEvent::getGallonsUsed).sum();
        assertThat(eventTotal).isLessThanOrEqualTo(history.gallonsUsed() + 0.01);
    }
}
