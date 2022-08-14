package com.july.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.july.reggie.commom.R;
import com.july.reggie.dto.DishDto;
import com.july.reggie.dto.SetmealDto;
import com.july.reggie.entity.Category;
import com.july.reggie.entity.DishFlavor;
import com.july.reggie.entity.Setmeal;
import com.july.reggie.entity.SetmealDish;
import com.july.reggie.service.CategoryService;
import com.july.reggie.service.DishFlavorService;
import com.july.reggie.service.DishService;
import com.july.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setMealService.page(pageInfo, queryWrapper);
        Page<SetmealDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<SetmealDto> list = new ArrayList<>();
        for (Setmeal setmeal : pageInfo.getRecords()) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmealDto.setCategoryName(categoryService.getById(setmeal.getCategoryId()).getName());
            list.add(setmealDto);
        }
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 新增套餐
     *
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setMealCache", key = "#setmealDto.categoryId+'_'+#setmealDto.status")
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setMealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 查询套餐
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto dto = setMealService.getByIdWithDish(id);
        return R.success(dto);
    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setMealCache", key = "#setmealDto.categoryId+'_'+#setmealDto.status")
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setMealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }

    /**
     * 启停售卖套餐
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setMealCache", allEntries = true)
    public R<String> sale(@PathVariable int status, String[] ids) {
        for (String id : ids) {
            Setmeal setmeal = setMealService.getById(id);
            setmeal.setStatus(status);
            setMealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setMealCache", allEntries = true)
    public R<String> delete(String[] ids) {
        for (String id : ids) {
            Setmeal setMeal = setMealService.getById(id);
            setMealService.removeWithDish(setMeal);
        }
        return R.success("删除成功");
    }

    /**
     * 获取指定分类的在售套餐
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setMealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        Long categoryId = setmeal.getCategoryId();
        queryWrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, 1)
                .orderByDesc(Setmeal::getUpdateTime);
        return R.success(setMealService.list(queryWrapper));
    }

    /**
     * 获取指定套餐的所有菜品
     *
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable Long id) {
        SetmealDto setmealDto = setMealService.getByIdWithDish(id);
        List<DishDto> list = new ArrayList<>();
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            DishDto dishDto = dishService.getByIdWithFlavor(setmealDish.getDishId());
            dishDto.setCopies(setmealDish.getCopies());

            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, setmealDish.getDishId());
            dishDto.setFlavors(dishFlavorService.list(wrapper));
            list.add(dishDto);
        }
        return list.size() != 0 ? R.success(list) : R.error("不存在的菜品或套餐！");
    }
}
