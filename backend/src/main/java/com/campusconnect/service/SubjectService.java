package com.campusconnect.service;

import com.campusconnect.model.Subject;
import com.campusconnect.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(UUID id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    public Subject updateSubject(UUID id, Subject subject) {

        Subject existingSubject = getSubjectById(id);

        existingSubject.setName(subject.getName());
        existingSubject.setCode(subject.getCode());
        existingSubject.setSection(subject.getSection());

        return subjectRepository.save(existingSubject);
    }

    public void deleteSubject(UUID id) {

        Subject subject = getSubjectById(id);

        subjectRepository.delete(subject);
    }
}