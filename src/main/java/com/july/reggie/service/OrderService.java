package com.july.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.july.reggie.dto.OrdersDto;
import com.july.reggie.entity.Orders;

/**
 * @author july
 */
public interface OrderService extends IService<Orders> {

    /**
     * 提交订单和订单明细
     *
     * @param orders
     * @return
     */
    Orders submit(Orders orders);

    /**
     * 分页查询订单和订单明细
     *
     * @param page
     * @param pageSize
     * @return
     */
    Page<OrdersDto> pageWithDetails(int page, int pageSize);

    /**
     * 再来一单
     *
     * @param orders
     */
    void again(Orders orders);

    /**
     * 支付订单
     *
     * @param orders
     * @return
     */
    String pay(Orders orders);

    void cancel(Orders orders);
}
