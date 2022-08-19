package com.july.reggie.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.july.reggie.commom.R;
import com.july.reggie.dto.OrdersDto;
import com.july.reggie.entity.Orders;
import com.july.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Value("${ali-pay.ali-pay-public-key}")
    private String aliPayPublicKey;

    /**
     * 提交订单并跳转支付界面
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        Orders order = orderService.submit(orders);
        String payUrl = orderService.pay(order);
        return R.success(payUrl);
    }

    /**
     * 跳转支付界面
     *
     * @param orders
     * @return
     */
    @PostMapping("/toPay")
    public R<String> toPay(@RequestBody Orders orders) {
        return R.success(orderService.pay(orders));
    }

    /**
     * 取消订单
     *
     * @param orders
     * @return
     */
    @PostMapping("/cancel")
    public R<String> cancel(@RequestBody Orders orders) {
        orderService.cancel(orders);
        return R.success("取消成功");
    }

    /**
     * 支付订单后回调
     *
     * @param data
     * @return
     */
    @PostMapping("/pay")
    public String pay(@RequestParam Map<String, String> data) {
        log.info("收到支付宝回调：{}", data);
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(data, aliPayPublicKey, AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
            if(signVerified) {
                LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Orders::getNumber, data.get("out_trade_no"));
                Orders order = orderService.getOne(queryWrapper);
                synchronized(this){
                    if(order.getStatus().equals(1)){
                        //将订单设置为已支付状态
                        order.setStatus(2);
                        //更新订单状态
                        orderService.updateById(order);
                        log.info("订单{}的支付记录修改成功}.",order.getNumber());
                    }
                    else{
                        log.debug("订单{}状态为{},回调处理退出.",order.getNumber(), order.getStatus());
                    }
                }
            } else {
                log.error("验签失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("验签异常");
            return "failure";
        }
        return "success";
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

    /**
     * 分页查询订单
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
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

    /**
     * 派送订单
     *
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("操作成功");
    }
}
