package com.campusconnect.controller;

import com.campusconnect.dto.DepartmentRequest;
import com.campusconnect.model.Department;
import com.campusconnect.repository.DepartmentRepository;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/departments")
public class AdminDepartmentController {

    private final DepartmentRepository departmentRepository;

    public AdminDepartmentController(
            DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> createDepartment(
             @Valid @RequestBody DepartmentRequest request) {

        Department department = new Department();

        department.setName(request.getName());
        department.setCode(request.getCode());

        return ResponseEntity.ok(
                departmentRepository.save(department)
        );
    }
}
