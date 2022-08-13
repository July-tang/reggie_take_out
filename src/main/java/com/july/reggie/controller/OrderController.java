package com.july.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.july.reggie.commom.R;
import com.july.reggie.dto.OrdersDto;
import com.july.reggie.entity.Orders;
import com.july.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户订单分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize) {
        Page<OrdersDto> ordersDtoPage = orderService.pageWithDetails(page, pageSize);
        return R.success(ordersDtoPage);
    }

    /**
     * 再来一单
     *
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        orderService.again(orders);
        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(number), Orders::getId, number)
                .between(beginTime != null, Orders::getOrderTime, beginTime, endTime)
                .orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("操作成功");
    }
}
