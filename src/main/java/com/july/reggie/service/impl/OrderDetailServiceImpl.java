package com.july.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.entity.OrderDetail;
import com.july.reggie.mapper.OrderDetailMapper;
import com.july.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @author july
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
