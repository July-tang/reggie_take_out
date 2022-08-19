package com.july.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.july.reggie.entity.ShoppingCart;

/**
 * @author july
 */
public interface ShoppingCartService extends IService<ShoppingCart> {
    void saveIfAbsent(ShoppingCart shoppingCart);
}
