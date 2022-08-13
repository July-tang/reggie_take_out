package com.july.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.entity.ShoppingCart;
import com.july.reggie.mapper.ShoppingCartMapper;
import com.july.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @author july
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {


}
