# reggie_take_out
项目原型：黑马程序员项目瑞吉外卖，B站视频地址：https://www.bilibili.com/video/BV13a411q753/

本人修改如下：
1. 实现商品添加和下单接口的幂等性，修复重复添加商品导致的购物车bug和重复下单。
2. 引入RabbitMQ实现异步下单。
3. 引入Alipay实现订单支付。
4. 利用RabbitMQ延迟队列实现超时自动取消订单支付。
