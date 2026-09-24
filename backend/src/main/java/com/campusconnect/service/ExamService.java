package com.campusconnect.service;

import com.campusconnect.dto.ExamRequest;
import com.campusconnect.dto.ExamResponse;
import com.campusconnect.model.Exam;
import com.campusconnect.model.Section;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.ExamRepository;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;

    public ExamService(
            ExamRepository examRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository
    ) {
        this.examRepository = examRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
    }

    public ExamResponse createExam(ExamRequest request) {

        validateTime(request);

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found")
                );

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found")
                );

        validateSubjectBelongsToSection(subject, section);

        checkSectionConflict(request, null);

        Exam exam = new Exam();

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setMaximumMarks(request.getMaximumMarks());
        exam.setSection(section);
        exam.setSubject(subject);

        return toResponse(examRepository.save(exam));
    }

    public List<ExamResponse> getAllExams() {

        return examRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamResponse getExamById(UUID id) {

        Exam exam = examRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found")
                );

        return toResponse(exam);
    }

    public ExamResponse updateExam(
            UUID id,
            ExamRequest request
    ) {

        validateTime(request);

        Exam exam = examRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found")
                );

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found")
                );

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found")
                );

        validateSubjectBelongsToSection(subject, section);

        checkSectionConflict(request, id);

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setMaximumMarks(request.getMaximumMarks());
        exam.setSection(section);
        exam.setSubject(subject);

        return toResponse(examRepository.save(exam));
    }

    public void deleteExam(UUID id) {

        if (!examRepository.existsById(id)) {
            throw new RuntimeException("Exam not found");
        }

        examRepository.deleteById(id);
    }

    private void validateTime(ExamRequest request) {

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException(
                    "Exam end time must be after start time"
            );
        }
    }

    private void validateSubjectBelongsToSection(
            Subject subject,
            Section section
    ) {

        if (!subject.getSection().getId().equals(section.getId())) {
            throw new RuntimeException(
                    "Subject does not belong to the selected section"
            );
        }
    }

    private void checkSectionConflict(
            ExamRequest request,
            UUID currentExamId
    ) {

        boolean conflict;

        if (currentExamId == null) {

            conflict =
                    examRepository
                            .existsBySectionIdAndExamDateAndStartTimeLessThanAndEndTimeGreaterThan(
                                    request.getSectionId(),
                                    request.getExamDate(),
                                    request.getEndTime(),
                                    request.getStartTime()
                            );

        } else {

            conflict =
                    examRepository
                            .existsBySectionIdAndExamDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                                    request.getSectionId(),
                                    request.getExamDate(),
                                    request.getEndTime(),
                                    request.getStartTime(),
                                    currentExamId
                            );
        }

        if (conflict) {
            throw new RuntimeException(
                    "Exam timetable conflict: section already has an exam during this time"
            );
        }
    }

    private ExamResponse toResponse(Exam exam) {

        ExamResponse response = new ExamResponse();

        response.setId(exam.getId());
        response.setTitle(exam.getTitle());
        response.setDescription(exam.getDescription());
        response.setExamDate(exam.getExamDate());
        response.setStartTime(exam.getStartTime());
        response.setEndTime(exam.getEndTime());
        response.setMaximumMarks(exam.getMaximumMarks());

        response.setSectionId(
                exam.getSection().getId()
        );

        response.setSectionName(
                exam.getSection().getName()
        );

        response.setSubjectId(
                exam.getSubject().getId()
        );

        response.setSubjectName(
                exam.getSubject().getName()
        );

        response.setSubjectCode(
                exam.getSubject().getCode()
        );

        return response;
    }
    @Transactional
    public List<ExamResponse> createBulkExams(
            List<ExamRequest> requests
    ) {

        List<ExamResponse> responses = new java.util.ArrayList<>();

        for (ExamRequest request : requests) {
            responses.add(createExam(request));
        }

        return responses;
    }

}