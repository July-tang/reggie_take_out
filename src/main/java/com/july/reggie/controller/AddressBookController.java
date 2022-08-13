package com.july.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.july.reggie.commom.BaseContext;
import com.july.reggie.commom.R;
import com.july.reggie.entity.AddressBook;
import com.july.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;


    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
//        wrapper.eq(AddressBook::getIsDefault, 1);
        wrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询当前用户的所有地址
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户ID为：{}", userId);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, AddressBook::getUserId, userId);
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        return R.success(addressBookService.list(queryWrapper));
    }

    /**
     * 查询特定地址
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return addressBook != null ? R.success(addressBook) : R.error("没有找到地址对象！");
    }

    /**
     * 修改地址
     *
     */
    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        return addressBook != null ? R.success(addressBook) : R.error("没有找到地址对象！");
    }
}
