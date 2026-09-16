package com.xzkj.health.controller;

import com.xzkj.health.model.entity.Department;
import com.xzkj.health.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean DepartmentService departmentService;

    @Test
    void returnsDepartmentList() throws Exception {
        Department department = new Department();
        department.setId(1L);
        department.setDeptName("综采队");
        given(departmentService.list("采")).willReturn(List.of(department));

        mockMvc.perform(get("/department/list").param("keyword", "采"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].deptName").value("综采队"));
    }

    @Test
    void returnsEmptyListWhenNoDepartmentsMatch() throws Exception {
        given(departmentService.list("不存在的部门")).willReturn(List.of());

        mockMvc.perform(get("/department/list").param("keyword", "不存在的部门"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
