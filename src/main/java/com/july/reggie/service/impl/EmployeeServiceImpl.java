package com.july.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.entity.Employee;
import com.july.reggie.mapper.EmployeeMapper;
import com.july.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author july
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
