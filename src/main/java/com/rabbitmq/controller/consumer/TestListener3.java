package com.rabbitmq.controller.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener3 { // SpringBoot连接测试(发布订阅)

    /*@RabbitListener(queues = "yyds1")
    public void receiver1(String data) {
        System.out.println("一号消息队列监听器: " + data);
    }

    @RabbitListener(queues = "yyds2")
    public void receiver2(String data) {
        System.out.println("二号消息队列监听器: " + data);
    }*/

}
