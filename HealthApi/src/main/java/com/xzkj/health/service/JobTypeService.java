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

    public JobType create(JobType jobType) {
        if (jobType == null) {
            throw new IllegalArgumentException("岗位信息不能为空");
        }
        applyNameAndCode(jobType, jobType.getTypeName(), jobType.getTypeCode());
        jobType.setId(null);
        jobType.setRiskLevel(normalizeRiskLevel(jobType.getRiskLevel()));
        if (jobType.getStatus() == null) {
            jobType.setStatus(0);
        }
        jobTypeMapper.insert(jobType);
        return jobType;
    }

    public JobType update(JobType jobType) {
        if (jobType == null || jobType.getId() == null) {
            throw new IllegalArgumentException("岗位ID不能为空");
        }
        JobType existing = jobTypeMapper.selectById(jobType.getId());
        if (existing == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        applyNameAndCode(existing, jobType.getTypeName(), jobType.getTypeCode());
        existing.setRiskLevel(normalizeRiskLevel(jobType.getRiskLevel()));
        jobTypeMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("岗位ID不能为空");
        }
        JobType existing = jobTypeMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        jobTypeMapper.deleteById(id);
    }

    private void applyNameAndCode(JobType target, String typeName, String typeCode) {
        String normalizedName = trimToEmpty(typeName);
        String normalizedCode = trimToEmpty(typeCode);
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("岗位名称不能为空");
        }
        if (normalizedCode.isEmpty()) {
            throw new IllegalArgumentException("岗位编码不能为空");
        }
        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("岗位名称不能超过100个字符");
        }
        if (normalizedCode.length() > 50) {
            throw new IllegalArgumentException("岗位编码不能超过50个字符");
        }
        target.setTypeName(normalizedName);
        target.setTypeCode(normalizedCode);
    }

    private Integer normalizeRiskLevel(Integer riskLevel) {
        if (riskLevel == null || riskLevel < 1 || riskLevel > 3) {
            return 1;
        }
        return riskLevel;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
