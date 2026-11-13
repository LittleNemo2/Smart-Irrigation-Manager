package com.austinbordeaux.irrigation.web.api;

import com.austinbordeaux.irrigation.engine.RuleResult;
import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Role;
import com.austinbordeaux.irrigation.service.DecisionPreviewService;
import com.austinbordeaux.irrigation.service.NotFoundException;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.service.ZoneDecisionPreview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ZoneDecisionApiControllerTest {

    private final DecisionPreviewService previewService = mock(DecisionPreviewService.class);
    private final PropertyService propertyService = mock(PropertyService.class);
    private final AppUser hannah = new AppUser("homeowner", "hash", "Hannah", "h@example.com", Role.HOMEOWNER);
    private final Principal principal = () -> "homeowner";
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ZoneDecisionApiController(previewService, propertyService)).build();
        when(propertyService.currentUser(any())).thenReturn(hannah);
    }

    @Test
    @DisplayName("GET returns the decision and rule breakdown as JSON")
    void returnsDecisionAsJson() throws Exception {
        ZoneDecisionPreview preview = new ZoneDecisionPreview(2L, "Front Lawn", "Maple Street Home", 85, true,
                "high", "Will water at next scheduled time",
                List.of(new RuleResult("Soil moisture", 20, "Soil is 15%: below threshold, needs water")),
                true, LocalDateTime.of(2026, 11, 14, 6, 0));
        when(previewService.preview(1L, 2L, hannah)).thenReturn(preview);

        mvc.perform(get("/api/properties/1/zones/2/decision").principal(principal))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.zone").value("Front Lawn"))
                .andExpect(jsonPath("$.probability").value(85))
                .andExpect(jsonPath("$.willWater").value(true))
                .andExpect(jsonPath("$.rules[0].rule").value("Soil moisture"));
    }

    @Test
    @DisplayName("A zone the user can't see returns 404")
    void otherUsersZoneReturns404() throws Exception {
        when(previewService.preview(1L, 99L, hannah)).thenThrow(new NotFoundException("Zone", 99L));

        mvc.perform(get("/api/properties/1/zones/99/decision").principal(principal))
                .andExpect(status().isNotFound());
    }
}
