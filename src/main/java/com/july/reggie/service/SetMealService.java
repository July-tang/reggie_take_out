package com.july.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.july.reggie.dto.SetmealDto;
import com.july.reggie.entity.Setmeal;

/**
 * @author july
 */
public interface SetMealService extends IService<Setmeal> {

    /**
     * 保存套餐和菜品
     *
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 根据Id获取套餐和菜品
     *
     * @param id
     * @return
     */
    SetmealDto getByIdWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    void removeWithDish(Setmeal setmeal);
}
