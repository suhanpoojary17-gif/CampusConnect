package com.campusconnect.service;

import com.campusconnect.model.Semester;
import com.campusconnect.repository.SemesterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public SemesterService(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    public Semester createSemester(Semester semester) {
        return semesterRepository.save(semester);
    }

    public List<Semester> getAllSemesters() {
        return semesterRepository.findAll();
    }

    public Semester getSemesterById(UUID id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semester not found"));
    }

    public Semester updateSemester(UUID id, Semester semester) {
        Semester existingSemester = getSemesterById(id);

        existingSemester.setNumber(semester.getNumber());

        return semesterRepository.save(existingSemester);
    }

    public void deleteSemester(UUID id) {
        Semester semester = getSemesterById(id);
        semesterRepository.delete(semester);
    }
}