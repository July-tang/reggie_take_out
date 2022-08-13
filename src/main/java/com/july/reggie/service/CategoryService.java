package com.july.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.july.reggie.entity.Category;

/**
 * @author july
 */
public interface CategoryService extends IService<Category> {

    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     */
    public void remove(Long id);
}
