package com.july.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.july.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author july
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
