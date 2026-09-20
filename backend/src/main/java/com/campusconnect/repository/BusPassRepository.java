package com.campusconnect.repository;

import com.campusconnect.entity.BusPass;
import com.campusconnect.model.BusPassStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusPassRepository extends JpaRepository<BusPass, UUID> {

    List<BusPass> findByStudentId(Long studentId);

    List<BusPass> findByStatus(BusPassStatus status);

    boolean existsByStudentIdAndStatus(Long studentId, BusPassStatus status);
}