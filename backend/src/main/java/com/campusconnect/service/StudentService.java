package com.campusconnect.service;

import com.campusconnect.model.Student;
import com.campusconnect.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public Student updateStudent(UUID id, Student student) {
        Student existingStudent = getStudentById(id);

        existingStudent.setName(student.getName());
        existingStudent.setEmail(student.getEmail());
        existingStudent.setPassword(student.getPassword());
        existingStudent.setSection(student.getSection());

        return studentRepository.save(existingStudent);
    }

    public List<Student> getStudentsBySection(UUID sectionId) {
    return studentRepository.findBySectionId(sectionId);
    }
    
    public void deleteStudent(UUID id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }
}