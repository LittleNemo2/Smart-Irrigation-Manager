package com.austinbordeaux.irrigation.web.form;

import com.austinbordeaux.irrigation.model.Property;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PropertyForm {

    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must be 80 characters or fewer")
    @Pattern(regexp = "^[^<>]*$", message = "Name may not contain < or >")
    private String name;

    @Size(max = 160, message = "Address must be 160 characters or fewer")
    @Pattern(regexp = "^[^<>]*$", message = "Address may not contain < or >")
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @Size(max = 500, message = "Notes must be 500 characters or fewer")
    private String notes;

    public static PropertyForm from(Property p) {
        PropertyForm f = new PropertyForm();
        f.name = p.getName();
        f.address = p.getAddress();
        f.latitude = p.getLatitude();
        f.longitude = p.getLongitude();
        f.notes = p.getNotes();
        return f;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? null : notes.trim();
    }
}