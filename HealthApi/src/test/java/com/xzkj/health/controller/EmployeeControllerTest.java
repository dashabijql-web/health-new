package com.xzkj.health.controller;

import com.xzkj.health.model.dto.EmployeeListItem;
import com.xzkj.health.model.dto.EmployeePage;
import com.xzkj.health.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class EmployeeControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    EmployeeService employeeService;

    @Test
    void returnsEmployeePage() throws Exception {
        EmployeeListItem item = new EmployeeListItem();
        item.setId(155L);
        item.setEmpName("张伟");
        item.setEmpCode("EMP0001");
        item.setDeptName("综采一队");
        given(employeeService.list("张", 1, 20))
                .willReturn(new EmployeePage(List.of(item), 1, 1, 20));

        mockMvc.perform(get("/employee/list")
                        .param("keyword", "张")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.list[0].empName").value("张伟"))
                .andExpect(jsonPath("$.list[0].deptName").value("综采一队"));
    }
}
