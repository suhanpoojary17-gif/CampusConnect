package com.campusconnect.entity;

import com.campusconnect.model.Section;
import com.campusconnect.model.Subject;
import jakarta.persistence.*;

@Entity
@Table(name = "teacher_assignments")
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public TeacherAssignment() {
    }

    public TeacherAssignment(
            User teacher,
            Section section,
            Subject subject) {

        this.teacher = teacher;
        this.section = section;
        this.subject = subject;
    }

    public Long getId() {
        return id;
    }

    public User getTeacher() {
        return teacher;
    }

    public Section getSection() {
        return section;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}