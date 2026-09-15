package com.campusconnect.repository;

import com.campusconnect.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findBySectionId(UUID sectionId);
}