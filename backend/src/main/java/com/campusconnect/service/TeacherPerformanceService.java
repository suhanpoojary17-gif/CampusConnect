package com.campusconnect.service;

import com.campusconnect.dto.StudentPerformanceResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Assessment;
import com.campusconnect.model.Student;
import com.campusconnect.model.StudentMark;
import com.campusconnect.repository.AssessmentRepository;
import com.campusconnect.repository.StudentMarkRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TeacherPerformanceService {

    private final StudentMarkRepository studentMarkRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserRepository userRepository;

    public TeacherPerformanceService(
            StudentMarkRepository studentMarkRepository,
            AssessmentRepository assessmentRepository,
            StudentRepository studentRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            UserRepository userRepository
    ) {
        this.studentMarkRepository = studentMarkRepository;
        this.assessmentRepository = assessmentRepository;
        this.studentRepository = studentRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentPerformanceResponse> getStudentPerformance(
            UUID sectionId,
            UUID subjectId,
            String teacherEmail
    ) {

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (!teacher.getRole().name().equals("TEACHER")) {
            throw new RuntimeException(
                    "Only teachers can access this performance view"
            );
        }

        boolean assigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacher.getId(),
                                sectionId,
                                subjectId
                        );

        if (!assigned) {
            throw new RuntimeException(
                    "Teacher is not assigned to this section and subject"
            );
        }

        List<Student> students =
                studentRepository.findBySectionId(sectionId);

        List<Assessment> assessments =
                assessmentRepository
                        .findBySectionIdAndSubjectId(
                                sectionId,
                                subjectId
                        );

        Map<UUID, StudentPerformanceResponse> performanceMap =
                new LinkedHashMap<>();

        for (Student student : students) {

            StudentPerformanceResponse response =
                    new StudentPerformanceResponse();

            response.setStudentId(student.getId());
            response.setStudentName(student.getName());

            response.setTotalMarksObtained(
                    BigDecimal.ZERO
            );

            response.setTotalMaximumMarks(
                    BigDecimal.ZERO
            );

            performanceMap.put(
                    student.getId(),
                    response
            );
        }

        for (Assessment assessment : assessments) {

            List<StudentMark> marks =
                    studentMarkRepository
                            .findByAssessmentId(
                                    assessment.getId()
                            );

            for (StudentMark mark : marks) {

                StudentPerformanceResponse response =
                        performanceMap.get(
                                mark.getStudent().getId()
                        );

                if (response == null) {
                    continue;
                }

                response.setTotalMarksObtained(
                        response
                                .getTotalMarksObtained()
                                .add(mark.getMarks())
                );

                response.setTotalMaximumMarks(
                        response
                                .getTotalMaximumMarks()
                                .add(
                                        assessment.getMaximumMarks()
                                )
                );
            }
        }

        for (StudentPerformanceResponse response :
                performanceMap.values()) {

            BigDecimal percentage =
                    calculatePercentage(
                            response.getTotalMarksObtained(),
                            response.getTotalMaximumMarks()
                    );

            response.setPercentage(percentage);

            response.setPerformanceLevel(
                    getPerformanceLevel(percentage)
            );
        }

        return List.copyOf(performanceMap.values());
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

        if (percentage.compareTo(
                BigDecimal.valueOf(90)) >= 0) {

            return "Excellent";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(75)) >= 0) {

            return "Very Good";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(60)) >= 0) {

            return "Good";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(50)) >= 0) {

            return "Average";
        }

        return "Needs Improvement";
    }

    @Transactional(readOnly = true)
    public List<StudentPerformanceResponse> getAdminStudentPerformance(
            UUID sectionId,
            UUID subjectId
    ) {

        List<Student> students =
                studentRepository.findBySectionId(sectionId);

        List<Assessment> assessments =
                assessmentRepository
                        .findBySectionIdAndSubjectId(
                                sectionId,
                                subjectId
                        );

        Map<UUID, StudentPerformanceResponse> performanceMap =
                new LinkedHashMap<>();

        for (Student student : students) {

            StudentPerformanceResponse response =
                    new StudentPerformanceResponse();

            response.setStudentId(student.getId());
            response.setStudentName(student.getName());

            response.setTotalMarksObtained(
                    BigDecimal.ZERO
            );

            response.setTotalMaximumMarks(
                    BigDecimal.ZERO
            );

            performanceMap.put(
                    student.getId(),
                    response
            );
        }

        for (Assessment assessment : assessments) {

            List<StudentMark> marks =
                    studentMarkRepository
                            .findByAssessmentId(
                                    assessment.getId()
                            );

            for (StudentMark mark : marks) {

                StudentPerformanceResponse response =
                        performanceMap.get(
                                mark.getStudent().getId()
                        );

                if (response == null) {
                    continue;
                }

                response.setTotalMarksObtained(
                        response
                                .getTotalMarksObtained()
                                .add(mark.getMarks())
                );

                response.setTotalMaximumMarks(
                        response
                                .getTotalMaximumMarks()
                                .add(
                                        assessment.getMaximumMarks()
                                )
                );
            }
        }

        for (StudentPerformanceResponse response :
                performanceMap.values()) {

            BigDecimal percentage =
                    calculatePercentage(
                            response.getTotalMarksObtained(),
                            response.getTotalMaximumMarks()
                    );

            response.setPercentage(percentage);

            response.setPerformanceLevel(
                    getPerformanceLevel(percentage)
            );
        }

        return List.copyOf(
                performanceMap.values()
        );
    }
}