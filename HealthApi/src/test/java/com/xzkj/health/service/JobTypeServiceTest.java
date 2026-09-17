package com.xzkj.health.service;

import com.xzkj.health.mapper.JobTypeMapper;
import com.xzkj.health.model.entity.JobType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobTypeServiceTest {
    @Mock
    JobTypeMapper jobTypeMapper;

    @InjectMocks
    JobTypeService jobTypeService;

    @Test
    void createsJobTypeWhenNameAndCodeArePresent() {
        JobType jobType = new JobType();
        jobType.setTypeName(" 学习测试岗位 ");
        jobType.setTypeCode(" LEARN-JOB ");
        when(jobTypeMapper.insert(any(JobType.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, JobType.class).setId(21L);
            return 1;
        });

        JobType created = jobTypeService.create(jobType);

        assertEquals(21L, created.getId());
        assertEquals("学习测试岗位", created.getTypeName());
        assertEquals("LEARN-JOB", created.getTypeCode());
        assertEquals(1, created.getRiskLevel());
        assertEquals(0, created.getStatus());
        verify(jobTypeMapper).insert(jobType);
    }

    @Test
    void rejectsBlankJobTypeName() {
        JobType jobType = new JobType();
        jobType.setTypeName("  ");
        jobType.setTypeCode("LEARN-JOB");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jobTypeService.create(jobType));

        assertEquals("岗位名称不能为空", exception.getMessage());
        verify(jobTypeMapper, never()).insert(any(JobType.class));
    }

    @Test
    void updatesJobTypeWhenIdExists() {
        JobType existing = new JobType();
        existing.setId(21L);
        existing.setTypeName("学习测试岗位");
        existing.setTypeCode("LEARN-JOB");
        existing.setRiskLevel(1);
        existing.setStatus(0);
        when(jobTypeMapper.selectById(21L)).thenReturn(existing);
        when(jobTypeMapper.updateById(existing)).thenReturn(1);

        JobType patch = new JobType();
        patch.setId(21L);
        patch.setTypeName(" 学习测试岗位-改 ");
        patch.setTypeCode(" LEARN-JOB-U ");
        patch.setRiskLevel(2);

        JobType updated = jobTypeService.update(patch);

        assertEquals("学习测试岗位-改", updated.getTypeName());
        assertEquals("LEARN-JOB-U", updated.getTypeCode());
        assertEquals(2, updated.getRiskLevel());
        verify(jobTypeMapper).updateById(existing);
    }

    @Test
    void deletesJobTypeWhenIdExists() {
        JobType existing = new JobType();
        existing.setId(21L);
        when(jobTypeMapper.selectById(21L)).thenReturn(existing);
        when(jobTypeMapper.deleteById(21L)).thenReturn(1);

        jobTypeService.delete(21L);

        verify(jobTypeMapper).deleteById(21L);
    }

    @Test
    void rejectsDeleteWhenJobTypeDoesNotExist() {
        when(jobTypeMapper.selectById(99L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jobTypeService.delete(99L));

        assertEquals("岗位不存在", exception.getMessage());
        verify(jobTypeMapper, never()).deleteById(99L);
    }
}
