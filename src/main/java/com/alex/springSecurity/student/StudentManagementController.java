package com.alex.springSecurity.student;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("management/api/v1/students")
public class StudentManagementController {

    private static final List<Student> STUDENTS = Arrays.asList(
            new Student(1, "Goku"),
            new Student(2, "Vegeta"),
            new Student(3, "Gohan")
    );

    /** cio' che e' stato fatto in ApplicationSecurityConfig con .requestMatchers(),
     * puo' essere fatto anche con l'annotation @PreAuthorize
     *
     * hasRole('ROLE_') hasAnyRole('ROLE_', ...) hasAuthority('permission') hasAnyPermission('permission', ...)
     * */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ADMINTRAINEE')")
    public List<Student> getAllStudent() {
        return STUDENTS;
    }

    @PostMapping
    public void registerNewStudent(@RequestBody Student student) {
        System.out.println(student);
    }

    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAuthority('student:write')")
    public void deleteStudent(@PathVariable("studentId") Integer studentId) {
        System.out.println(studentId);
    }

    @PutMapping("/{studentId}")
    public void updateStudent(@PathVariable("studentId") Integer studentId, @RequestBody Student student) {
        System.out.println("with: " + studentId + "; update: " + student);
    }
}
