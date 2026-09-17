package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.EmployeeListItem;
import com.xzkj.health.model.dto.EmployeePage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeService employeeService;

    @Test
    void usesDefaultPageAndSizeWhenMissing() {
        when(employeeMapper.countList(null)).thenReturn(1000L);
        when(employeeMapper.findPage(null, 0, 20)).thenReturn(List.of(new EmployeeListItem()));

        EmployeePage page = employeeService.list("  ", null, null);

        assertEquals(1000L, page.getTotal());
        assertEquals(1, page.getPage());
        assertEquals(20, page.getSize());
        verify(employeeMapper).findPage(null, 0, 20);
    }

    @Test
    void capsPageSize() {
        when(employeeMapper.countList("张")).thenReturn(3L);
        when(employeeMapper.findPage("张", 0, 50)).thenReturn(List.of());

        EmployeePage page = employeeService.list("张", 1, 200);

        assertEquals(50, page.getSize());
        verify(employeeMapper).findPage("张", 0, 50);
    }
}
