package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    // 使用json序列化，进行消息转换
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 定制rabbitTemplate
    @PostConstruct  // config对象创建完成后，执行这个方法
    public void initRabbitTemplate() {

        // 设置消息抵达服务器，就ack
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm...correlationData:[" + correlationData + "]   ack:[" + ack + "] cause:[" + cause + "]");
            }
        });

        // 设置消息抵达队列ack, 只要消息没有投递给指定的队列，就触发这个回调失败
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("fail message:[" + message
                        + "]  replyCode:[" + replyCode
                        + "]  replyText:[" + replyText
                        + "]  exchange:["+exchange
                        + "]  routingKey:["+routingKey);
            }
        });
    }



}
