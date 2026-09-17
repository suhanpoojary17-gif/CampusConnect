package com.campusconnect.controller;

import com.campusconnect.model.Student;
import com.campusconnect.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(
            @Valid @RequestBody Student student) {

        return ResponseEntity.ok(
                studentService.createStudent(student)
        );
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {

        return ResponseEntity.ok(
                studentService.getAllStudents()
        );
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Student>> getStudentsBySection(
            @PathVariable UUID sectionId) {

        return ResponseEntity.ok(
                studentService.getStudentsBySection(sectionId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                studentService.getStudentById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody Student student) {

        return ResponseEntity.ok(
                studentService.updateStudent(id, student)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable UUID id) {

        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}