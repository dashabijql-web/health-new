package com.xzkj.health.service;

import com.xzkj.health.mapper.JobTypeMapper;
import com.xzkj.health.model.entity.JobType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobTypeService {
    private final JobTypeMapper jobTypeMapper;

    public JobTypeService(JobTypeMapper jobTypeMapper) {
        this.jobTypeMapper = jobTypeMapper;
    }

    public List<JobType> list(String keyword) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return jobTypeMapper.findList(normalized);
    }
}
