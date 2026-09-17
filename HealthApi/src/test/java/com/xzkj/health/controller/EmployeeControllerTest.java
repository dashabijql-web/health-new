package com.xzkj.health.controller;

import com.xzkj.health.model.dto.EmployeeListItem;
import com.xzkj.health.model.dto.EmployeePage;
import com.xzkj.health.model.entity.Employee;
import com.xzkj.health.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(RestExceptionHandler.class)
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

    @Test
    void createsEmployee() throws Exception {
        given(employeeService.create(any(Employee.class))).willAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(2000L);
            employee.setEmpName("学习测试人员");
            employee.setEmpCode("LEARN-EMP");
            return employee;
        });

        mockMvc.perform(post("/employee/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empName\":\"学习测试人员\",\"empCode\":\"LEARN-EMP\",\"gender\":1,\"deptId\":10,\"jobTypeId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2000))
                .andExpect(jsonPath("$.empCode").value("LEARN-EMP"));
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(employeeService.create(any(Employee.class)))
                .willThrow(new IllegalArgumentException("工号已存在"));

        mockMvc.perform(post("/employee/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empName\":\"张伟\",\"empCode\":\"EMP0001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("工号已存在"));
    }

    @Test
    void updatesEmployee() throws Exception {
        given(employeeService.update(any(Employee.class))).willAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(2000L);
            employee.setEmpName("学习测试人员-改");
            return employee;
        });

        mockMvc.perform(put("/employee/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2000,\"empName\":\"学习测试人员-改\",\"empCode\":\"LEARN-EMP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empName").value("学习测试人员-改"));
    }

    @Test
    void deletesEmployee() throws Exception {
        mockMvc.perform(delete("/employee/delete/2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    void rejectsDeleteWhenServiceValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("人员不存在")).when(employeeService).delete(99L);

        mockMvc.perform(delete("/employee/delete/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("人员不存在"));
    }
}
