package com.campusconnect.service;

import com.campusconnect.dto.StudentExamResultResponse;
import com.campusconnect.model.Exam;
import com.campusconnect.model.ExamMark;
import com.campusconnect.model.ExamResultStatus;
import com.campusconnect.model.Student;
import com.campusconnect.repository.ExamMarkRepository;
import com.campusconnect.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class StudentExamResultService {

    private final ExamMarkRepository examMarkRepository;
    private final StudentRepository studentRepository;

    public StudentExamResultService(
            ExamMarkRepository examMarkRepository,
            StudentRepository studentRepository
    ) {
        this.examMarkRepository = examMarkRepository;
        this.studentRepository = studentRepository;
    }

    public List<StudentExamResultResponse> getMyResults(
            String email
    ) {

        Student student = studentRepository.findByUserEmail(email);

        if (student == null) {
            throw new RuntimeException(
                    "Student profile not found"
            );
        }

        return examMarkRepository
                .findByStudentId(student.getId())
                .stream()
                .filter(examMark ->
                        examMark.getExam()
                                .getResultStatus()
                                == ExamResultStatus.PUBLISHED
                )
                .sorted((a, b) -> {
                    Exam examA = a.getExam();
                    Exam examB = b.getExam();

                    int dateCompare =
                            examB.getExamDate()
                                    .compareTo(examA.getExamDate());

                    if (dateCompare != 0) {
                        return dateCompare;
                    }

                    return examA.getStartTime()
                            .compareTo(examB.getStartTime());
                })
                .map(this::toResponse)
                .toList();
    }

    private StudentExamResultResponse toResponse(
            ExamMark examMark
    ) {

        Exam exam = examMark.getExam();

        StudentExamResultResponse response =
                new StudentExamResultResponse();

        response.setExamId(exam.getId());

        response.setExamTitle(
                exam.getTitle()
        );

        response.setExamDate(
                exam.getExamDate()
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

        response.setMaximumMarks(
                exam.getMaximumMarks()
        );

        response.setMarksObtained(
                examMark.getMarks()
        );

        BigDecimal percentage =
                examMark.getMarks()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                exam.getMaximumMarks(),
                                2,
                                RoundingMode.HALF_UP
                        );

        response.setPercentage(
                percentage
        );

        response.setGradedAt(
                examMark.getGradedAt()
        );

        return response;
    }
}