package com.austinbordeaux.irrigation.repository;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WateringEventRepository extends JpaRepository<WateringEvent, Long> {

    List<WateringEvent> findTop10ByZone_PropertyOrderByRunAtDesc(Property property);

    @Query("""
            select e from WateringEvent e
              join e.zone z
              join z.property p
            where (:admin = true or p.owner = :owner)
              and (:propertyId is null or p.id = :propertyId)
              and (:zone = '' or lower(z.name) like lower(concat('%', :zone, '%')))
              and (:outcome is null or e.outcome = :outcome)
              and e.runAt >= :from and e.runAt < :to
            order by e.runAt desc
            """)
    List<WateringEvent> search(@Param("owner") AppUser owner,
                               @Param("admin") boolean admin,
                               @Param("propertyId") Long propertyId,
                               @Param("zone") String zone,
                               @Param("outcome") WateringOutcome outcome,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);
}