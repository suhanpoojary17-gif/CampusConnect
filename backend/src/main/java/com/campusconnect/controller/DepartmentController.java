package com.campusconnect.controller;

import com.campusconnect.model.Department;
import com.campusconnect.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody Department department) {

        return ResponseEntity.ok(
                departmentService.createDepartment(department)
        );
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {

        return ResponseEntity.ok(
                departmentService.getAllDepartments()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                departmentService.getDepartmentById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody Department department) {

        return ResponseEntity.ok(
                departmentService.updateDepartment(id, department)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable UUID id) {

        departmentService.deleteDepartment(id);

        return ResponseEntity.noContent().build();
    }
}