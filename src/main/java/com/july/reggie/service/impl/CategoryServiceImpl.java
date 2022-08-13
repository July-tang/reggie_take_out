package com.july.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.entity.Category;
import com.july.reggie.entity.Dish;
import com.july.reggie.entity.Setmeal;
import com.july.reggie.exception.CustomException;
import com.july.reggie.mapper.CategoryMapper;
import com.july.reggie.service.CategoryService;
import com.july.reggie.service.DishService;
import com.july.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author july
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetMealService setmealService;

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dishLambdaQueryWrapper);

        if (count > 0) {
            //已经关联菜品
            throw new CustomException("当前分类已经关联菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if (count2 > 0) {
            //已经关联套餐
            throw new CustomException("当前分类已经关联套餐，不能删除");
        }

        super.removeById(id);
    }
}
