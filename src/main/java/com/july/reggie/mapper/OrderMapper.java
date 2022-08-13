package com.july.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.july.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author july
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
