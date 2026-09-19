package com.campusconnect.repository;

import com.campusconnect.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {

    List<Notice> findByTargetSectionId(UUID sectionId);

    List<Notice> findByCreatorId(Long creatorId);

}