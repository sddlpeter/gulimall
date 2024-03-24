package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;


@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    // 1. Message ampq.core package
    // 2. T<发送的消息类型>
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        byte[] body = message.getBody();
        System.out.println("接收到消息..." + " content : " + content);

        MessageProperties properties = message.getMessageProperties();
        // Thread.sleep(3000);
        System.out.println("消息处理完成...." + content.getName());

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 签收消息
        try {
            if (deliveryTag % 2 == 0) {
                // 收货
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物..." + deliveryTag);
            } else {
                // 退货
                channel.basicNack(deliveryTag, false, false);
                System.out.println("货物未签收..." + deliveryTag);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @RabbitHandler
    public void receiveMessage2(OrderEntity content) throws InterruptedException {

        System.out.println("接收到消息..." + " Entity : " + content);


    }

}