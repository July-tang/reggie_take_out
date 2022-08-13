package com.july.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.commom.BaseContext;
import com.july.reggie.dto.OrdersDto;
import com.july.reggie.entity.*;
import com.july.reggie.exception.CustomException;
import com.july.reggie.mapper.OrderMapper;
import com.july.reggie.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author july
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，提交订单失败！");
        }

        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，提交订单失败！");
        }

        User user = userService.getById(userId);
        long orderId = IdWorker.getId();
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(orderId);
            setOrderDetailParas(orderDetail, shoppingCart);
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());

            orderDetails.add(orderDetail);
        }
        //设置订单参数
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //保存订单
        this.save(orders);

        //保存订单明细
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    @Override
    @Transactional
    public Page<OrdersDto> pageWithDetails(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId())
                .orderByDesc(Orders::getOrderTime);
        this.page(pageInfo, queryWrapper);

        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<OrdersDto> dtoList = new ArrayList<>();
        for (Orders orders : pageInfo.getRecords()) {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders, ordersDto);

            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, orders.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper);
            int num = 0;
            for (OrderDetail orderDetail : orderDetails) {
                num += orderDetail.getNumber();
            }
            ordersDto.setOrderDetails(orderDetails);
            ordersDto.setSumNum(num);
            dtoList.add(ordersDto);
        }
        ordersDtoPage.setRecords(dtoList);
        return ordersDtoPage;
    }

    @Override
    @Transactional
    public void again(Orders orders) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        //先清空购物车数据
        shoppingCartService.remove(queryWrapper);

        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getOrderId, orders.getId());
        List<OrderDetail> orderDetails = orderDetailService.list(wrapper);
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            setShoppingCartParas(orderDetail, shoppingCart);
            shoppingCartService.save(shoppingCart);
        }
    }

    /**
     * 设置订单明细参数
     *
     * @param orderDetail
     * @param shoppingCart
     */
    private void setOrderDetailParas(OrderDetail orderDetail, ShoppingCart shoppingCart) {
        orderDetail.setNumber(shoppingCart.getNumber());
        orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
        orderDetail.setDishId(shoppingCart.getDishId());
        orderDetail.setSetmealId(shoppingCart.getSetmealId());
        orderDetail.setName(shoppingCart.getName());
        orderDetail.setImage(shoppingCart.getImage());
        orderDetail.setAmount(shoppingCart.getAmount());
    }

    /**
     * 设置购物车参数
     *
     * @param orderDetail
     * @param shoppingCart
     */
    private void setShoppingCartParas(OrderDetail orderDetail, ShoppingCart shoppingCart) {
        shoppingCart.setNumber(orderDetail.getNumber());
        shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
        shoppingCart.setDishId(orderDetail.getDishId());
        shoppingCart.setSetmealId(orderDetail.getSetmealId());
        shoppingCart.setName(orderDetail.getName());
        shoppingCart.setImage(orderDetail.getImage());
        shoppingCart.setAmount(orderDetail.getAmount());
    }

}
