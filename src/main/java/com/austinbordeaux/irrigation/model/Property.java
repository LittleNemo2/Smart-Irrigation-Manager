package com.austinbordeaux.irrigation.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "property")
public class Property extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 160)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name ASC")
    private List<Zone> zones = new ArrayList<>();

    protected Property() {
    }

    public Property(String name, String address, double latitude, double longitude, AppUser owner) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.owner = owner;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public void addZone(Zone zone) {
        zones.add(zone);
        zone.assignProperty(this);
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
    }

    public boolean isOwnedBy(AppUser user) {
        return owner != null && user != null
                && owner.getId() != null && owner.getId().equals(user.getId());
    }
    public boolean isOnline() { return !zones.isEmpty() && zones.stream().allMatch(Zone::isOnline); }

    public List<Zone> getZones() {
        return Collections.unmodifiableList(zones);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppUser getOwner() {
        return owner;
    }
}