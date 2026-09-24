package com.campusconnect.service;

import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.model.NotificationType;
import com.campusconnect.model.Student;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            StudentRepository studentRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    /*
     * Create a normal notification.
     */
    public Notification createNotification(
            Long userId,
            String title,
            String message,
            NotificationType type) {

        return createNotification(
                userId,
                title,
                message,
                type,
                null
        );
    }

    /*
     * Create a notification with an optional event key.
     *
     * If eventKey already exists for this user,
     * a duplicate notification will not be created.
     */
    public Notification createNotification(
            Long userId,
            String title,
            String message,
            NotificationType type,
            String eventKey) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        /*
         * Prevent duplicate event notifications.
         */
        if (eventKey != null &&
                notificationRepository
                        .existsByUserIdAndEventKey(
                                userId,
                                eventKey
                        )) {

            return null;
        }

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setEventKey(eventKey);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /*
     * Notify every student belonging to a section.
     */
    public void notifyStudentsInSection(
            UUID sectionId,
            String title,
            String message,
            NotificationType type) {

        notifyStudentsInSection(
                sectionId,
                title,
                message,
                type,
                null
        );
    }

    /*
     * Notify every student in a section with
     * duplicate-event protection.
     */
    public void notifyStudentsInSection(
            UUID sectionId,
            String title,
            String message,
            NotificationType type,
            String eventKeyPrefix) {

        List<Student> students =
                studentRepository.findBySectionId(sectionId);

        for (Student student : students) {

            User user = student.getUser();

            if (user == null) {
                continue;
            }

            String eventKey = null;

            if (eventKeyPrefix != null) {
                eventKey =
                        eventKeyPrefix + ":" + user.getId();
            }

            createNotification(
                    user.getId(),
                    title,
                    message,
                    type,
                    eventKey
            );
        }
    }

    /*
     * Get all notifications belonging to a user.
     */
    public List<Notification> getUserNotifications(
            Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    /*
     * Get unread notification count.
     */
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    /*
     * Mark a notification as read.
     *
     * A user can only modify their own notification.
     */
    public Notification markAsRead(
            UUID notificationId,
            Long userId) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        if (!notification.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You are not allowed to access this notification"
            );
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }
}