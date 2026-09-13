package com.campusconnect.repository;

import com.campusconnect.model.Year;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface YearRepository extends MongoRepository<Year, String> {
}