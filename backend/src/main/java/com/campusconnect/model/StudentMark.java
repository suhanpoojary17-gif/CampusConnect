package com.campusconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "student_marks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_assessment_student",
            columnNames = {"assessment_id", "student_id"}
        )
    }
)
@Getter
@Setter
public class StudentMark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal marks;

    @Column(nullable = false)
    private LocalDateTime gradedAt;

    @PrePersist
    protected void onCreate() {
        gradedAt = LocalDateTime.now();
    }
}