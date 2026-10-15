package com.austinbordeaux.irrigation.repository;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("""
            select p from Property p
            where p.owner = :owner
              and (lower(p.name) like lower(concat('%', :keyword, '%'))
                   or lower(p.address) like lower(concat('%', :keyword, '%')))
            order by p.name
            """)
    List<Property> searchForOwner(@Param("owner") AppUser owner, @Param("keyword") String keyword);

    @Query("""
            select p from Property p
            where lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(p.address) like lower(concat('%', :keyword, '%'))
            order by p.name
            """)
    List<Property> searchAll(@Param("keyword") String keyword);
}