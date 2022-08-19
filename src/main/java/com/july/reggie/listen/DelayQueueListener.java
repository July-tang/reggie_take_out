package com.july.reggie.listen;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.july.reggie.config.RabbitConfig;
import com.july.reggie.entity.Orders;
import com.july.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author july
 */
@Slf4j
@Component
public class DelayQueueListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = RabbitConfig.DELAYED_QUEUE_NAME)
    public void receiveDelayQueue(Message message) {
        String msg = new String(message.getBody());
        log.info("当前时间：{}， 取消订单号{}的订单", new Date(), msg);
        Orders order = orderService.getById(msg);
        if (order.getStatus() == 1) {
            order.setStatus(5);
            orderService.updateById(order);
        }
    }
}
