package com.campusconnect.service;

import com.campusconnect.dto.ExamResponse;
import com.campusconnect.model.Exam;
import com.campusconnect.model.Student;
import com.campusconnect.repository.ExamRepository;
import com.campusconnect.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentExamService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;

    public StudentExamService(
            ExamRepository examRepository,
            StudentRepository studentRepository
    ) {
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
    }

    public List<ExamResponse> getMyExamTimetable(String email) {

        Student student = studentRepository.findByUserEmail(email);

        if (student == null) {
            throw new RuntimeException("Student profile not found");
        }

        return examRepository
                .findBySectionId(student.getSection().getId())
                .stream()
                .sorted((a, b) -> {

                    int dateCompare =
                            a.getExamDate()
                                    .compareTo(b.getExamDate());

                    if (dateCompare != 0) {
                        return dateCompare;
                    }

                    return a.getStartTime()
                            .compareTo(b.getStartTime());
                })
                .map(this::toResponse)
                .toList();
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

        response.setSectionId(exam.getSection().getId());
        response.setSectionName(exam.getSection().getName());

        response.setSubjectId(exam.getSubject().getId());
        response.setSubjectName(exam.getSubject().getName());
        response.setSubjectCode(exam.getSubject().getCode());

        return response;
    }
}