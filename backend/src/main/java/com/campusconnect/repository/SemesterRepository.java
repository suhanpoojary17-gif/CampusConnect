package com.campusconnect.repository;

import com.campusconnect.model.Semester;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SemesterRepository extends MongoRepository<Semester, String> {
}