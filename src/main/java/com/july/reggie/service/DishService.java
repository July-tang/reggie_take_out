package com.july.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.july.reggie.dto.DishDto;
import com.july.reggie.entity.Dish;

/**
 * @author july
 */
public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    void removeWithFlavor(Dish dish);
}
