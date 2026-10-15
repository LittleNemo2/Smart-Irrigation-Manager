package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.repository.AppUserRepository;
import com.austinbordeaux.irrigation.repository.PropertyRepository;
import com.austinbordeaux.irrigation.web.form.PropertyForm;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository properties;
    private final AppUserRepository users;

    public PropertyService(PropertyRepository properties, AppUserRepository users) {
        this.properties = properties;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public AppUser currentUser(Principal principal) {
        return users.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("Unknown user"));
    }

    @Transactional(readOnly = true)
    public List<Property> search(AppUser user, String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        return user.isAdmin() ? properties.searchAll(k) : properties.searchForOwner(user, k);
    }

    @Transactional(readOnly = true)
    public Property get(Long id, AppUser user) {
        Property property = properties.findById(id)
                .orElseThrow(() -> new NotFoundException("Property", id));
        if (!user.isAdmin() && !property.isOwnedBy(user)) {
            throw new NotFoundException("Property", id);
        }
        return property;
    }

    public Property create(PropertyForm form, AppUser owner) {
        Property property = new Property(form.getName(), form.getAddress(),
                form.getLatitude(), form.getLongitude(), owner);
        property.setNotes(form.getNotes());
        return properties.save(property);
    }

    public Property update(Long id, PropertyForm form, AppUser user) {
        Property property = get(id, user);
        property.setName(form.getName());
        property.setAddress(form.getAddress());
        property.setLatitude(form.getLatitude());
        property.setLongitude(form.getLongitude());
        property.setNotes(form.getNotes());
        return property;
    }

    public void delete(Long id, AppUser user) {
        properties.delete(get(id, user));
    }
}