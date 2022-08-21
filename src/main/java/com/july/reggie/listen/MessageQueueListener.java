package com.july.reggie.listen;

import com.july.reggie.commom.SseEmitterServer;
import com.july.reggie.config.RabbitConfig;
import com.july.reggie.entity.Orders;
import com.july.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Date;

/**
 * @author july
 */
@Slf4j
@Component
public class MessageQueueListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = RabbitConfig.DELAYED_QUEUE_NAME)
    public void receiveDelayQueue(Message message) {
        String orderNumber = new String(message.getBody());
        log.info("当前时间：{}， 取消订单号{}的订单", new Date(), orderNumber);
        Orders order = orderService.getById(orderNumber);
        if (order != null && order.getStatus() == 1) {
            order.setStatus(5);
            orderService.updateById(order);
        }
    }

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE_NAME)
    public void receiveOrderQueue(Message message) {
        ByteArrayInputStream bis = new ByteArrayInputStream (message.getBody());
        Orders order = null;
        try {
            //反序列化对象
            ObjectInputStream ois = new ObjectInputStream (bis);
            order = (Orders) ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("收到订单队列的消息：{}", order);
        Orders orderWithDetails = orderService.submitOrder(order);
        SseEmitterServer.sendMessage("add_order", orderWithDetails);
    }
}
