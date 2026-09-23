package com.campusconnect.service;

import com.campusconnect.dto.AcademicPerformanceResponse;
import com.campusconnect.dto.AssessmentPerformanceResponse;
import com.campusconnect.dto.SubjectPerformanceResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Student;
import com.campusconnect.model.StudentMark;
import com.campusconnect.repository.StudentMarkRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AcademicPerformanceService {

    private final StudentMarkRepository studentMarkRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public AcademicPerformanceService(
            StudentMarkRepository studentMarkRepository,
            StudentRepository studentRepository,
            UserRepository userRepository
    ) {
        this.studentMarkRepository = studentMarkRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AcademicPerformanceResponse getMyPerformance(
            String userEmail
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (!user.getRole().name().equals("STUDENT")) {
            throw new RuntimeException(
                    "Only students can access their academic performance"
            );
        }

        Student student = studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            throw new RuntimeException(
                    "Student profile not found"
            );
        }

        List<StudentMark> marks =
                studentMarkRepository.findByStudentId(student.getId());

        return calculatePerformance(marks);
    }

    private AcademicPerformanceResponse calculatePerformance(
            List<StudentMark> marks
    ) {

        AcademicPerformanceResponse response =
                new AcademicPerformanceResponse();

        BigDecimal totalMarksObtained = BigDecimal.ZERO;
        BigDecimal totalMaximumMarks = BigDecimal.ZERO;

        Map<UUID, SubjectPerformanceResponse> subjectMap =
                new LinkedHashMap<>();

        for (StudentMark mark : marks) {

            BigDecimal studentMarks = mark.getMarks();

            BigDecimal maximumMarks =
                    mark.getAssessment().getMaximumMarks();

            totalMarksObtained =
                    totalMarksObtained.add(studentMarks);

            totalMaximumMarks =
                    totalMaximumMarks.add(maximumMarks);

            UUID subjectId =
                    mark.getAssessment()
                            .getSubject()
                            .getId();

            SubjectPerformanceResponse subjectPerformance =
                    subjectMap.get(subjectId);

            if (subjectPerformance == null) {

                subjectPerformance =
                        new SubjectPerformanceResponse();

                subjectPerformance.setSubjectId(subjectId);

                subjectPerformance.setSubjectName(
                        mark.getAssessment()
                                .getSubject()
                                .getName()
                );

                subjectPerformance.setSubjectCode(
                        mark.getAssessment()
                                .getSubject()
                                .getCode()
                );

                subjectPerformance.setTotalMarksObtained(
                        BigDecimal.ZERO
                );

                subjectPerformance.setTotalMaximumMarks(
                        BigDecimal.ZERO
                );

                subjectPerformance.setAssessments(
                        new ArrayList<>()
                );

                subjectMap.put(
                        subjectId,
                        subjectPerformance
                );
            }

            // Subject totals
            subjectPerformance.setTotalMarksObtained(
                    subjectPerformance
                            .getTotalMarksObtained()
                            .add(studentMarks)
            );

            subjectPerformance.setTotalMaximumMarks(
                    subjectPerformance
                            .getTotalMaximumMarks()
                            .add(maximumMarks)
            );

            // Assessment details
            AssessmentPerformanceResponse assessment =
                    new AssessmentPerformanceResponse();

            assessment.setAssessmentId(
                    mark.getAssessment().getId()
            );

            assessment.setAssessmentTitle(
                    mark.getAssessment().getTitle()
            );

            assessment.setAssessmentType(
                    mark.getAssessment()
                            .getType()
                            .name()
            );

            assessment.setAssessmentDate(
                    mark.getAssessment()
                            .getAssessmentDate()
            );

            assessment.setMarksObtained(
                    studentMarks
            );

            assessment.setMaximumMarks(
                    maximumMarks
            );

            assessment.setPercentage(
                    calculatePercentage(
                            studentMarks,
                            maximumMarks
                    )
            );

            subjectPerformance.getAssessments()
                    .add(assessment);
        }

        // Calculate subject percentages
        for (SubjectPerformanceResponse subject :
                subjectMap.values()) {

            BigDecimal percentage =
                    calculatePercentage(
                            subject.getTotalMarksObtained(),
                            subject.getTotalMaximumMarks()
                    );

            subject.setPercentage(percentage);

            subject.setPerformanceLevel(
                getPerformanceLevel(percentage)
        );
        }

        response.setTotalMarksObtained(
                totalMarksObtained
        );

        response.setTotalMaximumMarks(
                totalMaximumMarks
        );

        BigDecimal overallPercentage =
        calculatePercentage(
                totalMarksObtained,
                totalMaximumMarks
        );

        response.setOverallPercentage(
                overallPercentage
        );

        response.setPerformanceLevel(
                getPerformanceLevel(overallPercentage)
        );

        response.setSubjects(
                new ArrayList<>(subjectMap.values())
        );

        return response;
    }

    private BigDecimal calculatePercentage(
            BigDecimal obtained,
            BigDecimal maximum
    ) {

        if (maximum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return obtained
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        maximum,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private String getPerformanceLevel(
            BigDecimal percentage
    ) {

        if (percentage.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "Excellent";
        }

        if (percentage.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "Very Good";
        }

        if (percentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "Good";
        }

        if (percentage.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "Average";
        }

        return "Needs Improvement";
    }    

}