package com.july.reggie.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.july.reggie.commom.BaseContext;
import com.july.reggie.dto.OrdersDto;
import com.july.reggie.entity.*;
import com.july.reggie.exception.CustomException;
import com.july.reggie.mapper.OrderMapper;
import com.july.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author july
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AlipayClient alipayClient;

    @Value("${ali-pay.notify-url}")
    private String notifyUrl;

    @Value("${ali-pay.return-url}")
    private String returnUrl;

    @Override
    @Transactional
    public Orders submit(Orders orders) {
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
        orders.setStatus(1);
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

        return orders;
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

    @Override
    public String pay(Orders orders) {
        return this.createPay(orders);
    }

    @Override
    public void cancel(Orders orders) {
        Orders order = this.getById(orders.getId());
        //未付款，需要取消支付
        if (order.getStatus() == 1) {
            if(cancelPay(order.getNumber())) {
                order.setStatus(5);
                this.updateById(order);
            }
        } else {
            order.setStatus(5);
            this.updateById(order);
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

    private String createPay(Orders orders) {
        //请求
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        //数据
        AlipayTradeWapPayModel bizModel = new AlipayTradeWapPayModel();
        bizModel.setSubject(orders.getOrderTime().toString());
        bizModel.setOutTradeNo(orders.getNumber());
        //单位是元
        bizModel.setTotalAmount(orders.getAmount()
                .toString());
        //默认的
        bizModel.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(bizModel);
        request.setNotifyUrl(notifyUrl);
        //用户支付后支付宝会以GET方法请求returnUrl,并且携带out_trade_no,trade_no,total_amount等参数.

        request.setReturnUrl(returnUrl);
        AlipayTradeWapPayResponse response = null;
        try {
            //完成签名并执行请求
            response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                log.debug("调用成功");
                return response.getBody();
            }
            else {
                log.error("调用失败");
                log.error(response.getMsg());
                return null;
            }
        }
        catch (AlipayApiException e) {
            log.error("调用异常");
            return null;
        }
    }

    private boolean cancelPay(String orderNumber){
        //请求
        AlipayTradeCloseRequest request=new AlipayTradeCloseRequest();
        //数据
        AlipayTradeCloseModel bizModel=new AlipayTradeCloseModel();
        bizModel.setOutTradeNo(orderNumber);
        request.setBizModel(bizModel);
        try{
            //完成签名并执行请求
            AlipayTradeCloseResponse response=alipayClient.execute(request);
            if(response.isSuccess()){
                log.debug("订单{}取消成功", orderNumber);
            }
            else{
                log.debug("订单{}未创建,因此也可认为本次取消成功.", orderNumber);
            }
            return true;
        }
        catch(AlipayApiException e){
            log.error("订单{}取消异常",orderNumber);
            return false;
        }
    }
}
