package com.campusconnect.service;

import com.campusconnect.model.Teacher;
import com.campusconnect.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher getTeacherById(UUID id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
    }

    public Teacher updateTeacher(UUID id, Teacher teacher) {
        Teacher existingTeacher = getTeacherById(id);

        existingTeacher.setName(teacher.getName());
        existingTeacher.setEmail(teacher.getEmail());
        existingTeacher.setPassword(teacher.getPassword());

        return teacherRepository.save(existingTeacher);
    }

    public void deleteTeacher(UUID id) {
        Teacher teacher = getTeacherById(id);
        teacherRepository.delete(teacher);
    }
}