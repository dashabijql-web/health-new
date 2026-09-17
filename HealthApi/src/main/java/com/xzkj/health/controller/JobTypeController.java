package com.xzkj.health.controller;

import com.xzkj.health.model.entity.JobType;
import com.xzkj.health.service.JobTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/job-type")
public class JobTypeController {
    private final JobTypeService jobTypeService;

    public JobTypeController(JobTypeService jobTypeService) {
        this.jobTypeService = jobTypeService;
    }

    @GetMapping("/list")
    public List<JobType> list(@RequestParam(required = false) String keyword) {
        return jobTypeService.list(keyword);
    }
}
