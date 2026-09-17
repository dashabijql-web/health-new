package com.xzkj.health.controller;

import com.xzkj.health.model.entity.JobType;
import com.xzkj.health.service.JobTypeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/create")
    public JobType create(@RequestBody JobType jobType) {
        return jobTypeService.create(jobType);
    }

    @PutMapping("/update")
    public JobType update(@RequestBody JobType jobType) {
        return jobTypeService.update(jobType);
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        jobTypeService.delete(id);
        return Map.of("message", "删除成功");
    }
}
