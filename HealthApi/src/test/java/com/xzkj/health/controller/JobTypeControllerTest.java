package com.xzkj.health.controller;

import com.xzkj.health.model.entity.JobType;
import com.xzkj.health.service.JobTypeService;
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

@WebMvcTest(JobTypeController.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class JobTypeControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    JobTypeService jobTypeService;

    @Test
    void returnsJobTypeList() throws Exception {
        JobType jobType = new JobType();
        jobType.setId(1L);
        jobType.setTypeName("综采工");
        given(jobTypeService.list("综")).willReturn(List.of(jobType));

        mockMvc.perform(get("/job-type/list").param("keyword", "综"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].typeName").value("综采工"));
    }

    @Test
    void returnsEmptyListWhenNoJobTypesMatch() throws Exception {
        given(jobTypeService.list("不存在的岗位")).willReturn(List.of());

        mockMvc.perform(get("/job-type/list").param("keyword", "不存在的岗位"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
