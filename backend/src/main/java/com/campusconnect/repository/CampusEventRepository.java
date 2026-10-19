package com.campusconnect.repository;

import com.campusconnect.entity.CampusEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampusEventRepository
        extends JpaRepository<CampusEvent, UUID> {

    List<CampusEvent> findByPublishedTrueOrderByStartDateTimeAsc();

    List<CampusEvent> findAllByOrderByStartDateTimeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM CampusEvent e WHERE e.id = :eventId")
    Optional<CampusEvent> findByIdForUpdate(
            @Param("eventId") UUID eventId
    );
}