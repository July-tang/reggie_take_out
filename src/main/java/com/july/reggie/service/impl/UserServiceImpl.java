package com.july.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.entity.User;
import com.july.reggie.mapper.UserMapper;
import com.july.reggie.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author july
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
