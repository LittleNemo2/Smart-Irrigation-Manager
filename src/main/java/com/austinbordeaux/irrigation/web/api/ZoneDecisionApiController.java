package com.austinbordeaux.irrigation.web.api;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.service.DecisionPreviewService;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.service.ZoneDecisionPreview;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * JSON API: GET /api/properties/{propertyId}/zones/{zoneId}/decision
 * returns the current watering decision for a zone the signed-in user owns.
 */
@RestController
public class ZoneDecisionApiController {

    private final DecisionPreviewService previewService;
    private final PropertyService propertyService;

    public ZoneDecisionApiController(DecisionPreviewService previewService, PropertyService propertyService) {
        this.previewService = previewService;
        this.propertyService = propertyService;
    }

    @GetMapping("/api/properties/{propertyId}/zones/{zoneId}/decision")
    public ZoneDecisionPreview decision(@PathVariable Long propertyId, @PathVariable Long zoneId,
                                        Principal principal) {
        AppUser user = propertyService.currentUser(principal);
        return previewService.preview(propertyId, zoneId, user);
    }
}
