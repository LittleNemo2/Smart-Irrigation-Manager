package com.austinbordeaux.irrigation.web.form;

import com.austinbordeaux.irrigation.model.Zone;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ZoneForm {

    @NotBlank(message = "Zone name is required")
    @Size(max = 60, message = "Zone name must be 60 characters or fewer")
    @Pattern(regexp = "^[^<>]*$", message = "Zone name may not contain < or >")
    private String name;

    @NotNull(message = "Choose a plant type")
    private Long plantTypeId;

    @NotNull(message = "Flow rate is required")
    @DecimalMin(value = "0.1", message = "Flow rate must be at least 0.1 GPM")
    @DecimalMax(value = "100.0", message = "Flow rate must be 100 GPM or less")
    private Double flowRateGpm;

    @NotNull(message = "Threshold is required")
    @DecimalMin(value = "0.0", message = "Threshold must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Threshold must be between 0 and 100")
    private Double moistureThreshold;

    @NotNull(message = "Target is required")
    @DecimalMin(value = "0.0", message = "Target must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Target must be between 0 and 100")
    private Double targetMoisture;

    private boolean online = true;

    @AssertTrue(message = "Threshold must be lower than the target")
    public boolean isThresholdBelowTarget() {
        if (moistureThreshold == null || targetMoisture == null) {
            return true;
        }
        return moistureThreshold < targetMoisture;
    }

    public static ZoneForm from(Zone zone) {
        ZoneForm f = new ZoneForm();
        f.name = zone.getName();
        f.plantTypeId = zone.getPlantType().getId();
        f.flowRateGpm = zone.getFlowRateGpm();
        f.moistureThreshold = zone.getMoistureThreshold();
        f.targetMoisture = zone.getTargetMoisture();
        f.online = zone.isOnline();
        return f;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Long getPlantTypeId() {
        return plantTypeId;
    }

    public void setPlantTypeId(Long plantTypeId) {
        this.plantTypeId = plantTypeId;
    }

    public Double getFlowRateGpm() {
        return flowRateGpm;
    }

    public void setFlowRateGpm(Double flowRateGpm) {
        this.flowRateGpm = flowRateGpm;
    }

    public Double getMoistureThreshold() {
        return moistureThreshold;
    }

    public void setMoistureThreshold(Double moistureThreshold) {
        this.moistureThreshold = moistureThreshold;
    }

    public Double getTargetMoisture() {
        return targetMoisture;
    }

    public void setTargetMoisture(Double targetMoisture) {
        this.targetMoisture = targetMoisture;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}