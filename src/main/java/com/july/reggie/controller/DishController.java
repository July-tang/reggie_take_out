package com.july.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.july.reggie.commom.R;
import com.july.reggie.dto.DishDto;
import com.july.reggie.entity.Category;
import com.july.reggie.entity.Dish;
import com.july.reggie.entity.DishFlavor;
import com.july.reggie.service.CategoryService;
import com.july.reggie.service.DishFlavorService;
import com.july.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);
        Page<DishDto> dishDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<DishDto> dishDtoList = new ArrayList<>();
        for (Dish dish : pageInfo.getRecords()) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setCategoryName(categoryService.getById(dish.getCategoryId()).getName());
            dishDtoList.add(dishDto);
        }
        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 启停售卖菜品
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> sale(@PathVariable int status, String[] ids) {
        for (String id : ids) {
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("操作成功");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String[] ids) {
        for (String id : ids) {
            dishService.removeWithFlavor(id);
        }
        return R.success("删除成功");
    }

    /**
     * 获取对应分类的所有菜品
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus,1)
                .eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<DishDto> dtoList = new ArrayList<>();
        for (Dish d : dishService.list(queryWrapper)) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d, dishDto);

            Category category = categoryService.getById(d.getCategoryId());
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, d.getId());
            dishDto.setFlavors(dishFlavorService.list(wrapper));

            dtoList.add(dishDto);
        }
        return R.success(dtoList);
    }
}
