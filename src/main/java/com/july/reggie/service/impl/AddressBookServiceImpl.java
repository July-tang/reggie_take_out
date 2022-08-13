package com.july.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.entity.AddressBook;
import com.july.reggie.mapper.AddressBookMapper;
import com.july.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * @author july
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
