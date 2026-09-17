package com.xzkj.health.controller;

import com.xzkj.health.model.entity.Department;
import com.xzkj.health.service.DepartmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/list")
    public List<Department> list(@RequestParam(required = false) String keyword) {
        return departmentService.list(keyword);
    }

    @PostMapping("/create")
    public Department create(@RequestBody Department department) {
        return departmentService.create(department);
    }

    @PutMapping("/update")
    public Department update(@RequestBody Department department) {
        return departmentService.update(department);
    }
}
