package com.campusconnect.controller;

import com.campusconnect.model.Teacher;
import com.campusconnect.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping
    public ResponseEntity<Teacher> createTeacher(
            @RequestBody Teacher teacher) {
        return ResponseEntity.ok(
                teacherService.createTeacher(teacher)
        );
    }

    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(
                teacherService.getAllTeachers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                teacherService.getTeacherById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(
            @PathVariable UUID id,
            @RequestBody Teacher teacher) {
        return ResponseEntity.ok(
                teacherService.updateTeacher(id, teacher)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(
            @PathVariable UUID id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}