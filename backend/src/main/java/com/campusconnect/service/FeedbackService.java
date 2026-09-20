package com.campusconnect.service;

import com.campusconnect.dto.FeedbackRequest;
import com.campusconnect.dto.FeedbackResponse;
import com.campusconnect.entity.Feedback;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.FeedbackRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponse submitFeedback(
            Long studentId,
            FeedbackRequest request) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        // Only students can submit feedback
        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException(
                    "Only students can submit feedback"
            );
        }

        if (request.getCategory() == null) {
            throw new RuntimeException(
                    "Feedback category is required"
            );
        }

        if (request.getMessage() == null ||
                request.getMessage().trim().isEmpty()) {

            throw new RuntimeException(
                    "Feedback message is required"
            );
        }

        Feedback feedback = new Feedback();

        feedback.setStudent(student);
        feedback.setCategory(request.getCategory());
        feedback.setMessage(request.getMessage());
        feedback.setAnonymous(request.isAnonymous());
        feedback.setCreatedAt(LocalDateTime.now());

        Feedback savedFeedback =
                feedbackRepository.save(feedback);

        return convertToResponse(savedFeedback, false);
    }

    public List<FeedbackResponse> getStudentFeedback(Long studentId) {

        return feedbackRepository.findByStudentId(studentId)
                .stream()
                .map(feedback ->
                        convertToResponse(feedback, false))
                .collect(Collectors.toList());
    }

    public List<FeedbackResponse> getAllFeedback() {

        return feedbackRepository.findAll()
                .stream()
                .map(feedback ->
                        convertToResponse(feedback, true))
                .collect(Collectors.toList());
    }

    public void deleteFeedback(java.util.UUID id) {

        if (!feedbackRepository.existsById(id)) {
            throw new RuntimeException(
                    "Feedback not found"
            );
        }

        feedbackRepository.deleteById(id);
    }

    private FeedbackResponse convertToResponse(
            Feedback feedback,
            boolean adminView) {

        FeedbackResponse response = new FeedbackResponse();

        response.setId(feedback.getId());
        response.setCategory(feedback.getCategory());
        response.setMessage(feedback.getMessage());
        response.setAnonymous(feedback.isAnonymous());
        response.setCreatedAt(feedback.getCreatedAt());

        /*
         * If feedback is anonymous and viewed by admin,
         * don't expose the student's identity.
         */
        if (!feedback.isAnonymous() || !adminView) {

            if (feedback.getStudent() != null) {
                response.setStudentId(
                        feedback.getStudent().getId()
                );

                response.setStudentName(
                        feedback.getStudent().getEmail()
                );
            }
        }

        return response;
    }
}