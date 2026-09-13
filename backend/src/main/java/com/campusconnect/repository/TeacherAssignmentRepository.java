package com.campusconnect.repository;

import com.campusconnect.model.TeacherAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeacherAssignmentRepository extends MongoRepository<TeacherAssignment, String> {
}
