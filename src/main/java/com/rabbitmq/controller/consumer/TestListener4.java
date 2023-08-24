package com.rabbitmq.controller.consumer;

import org.springframework.stereotype.Component;

@Component
public class TestListener4 { // SpringBoot连接测试(路由模式)

    /*@RabbitListener(queues = "yyds1")
    public void receiver1(String data) {
        System.out.println("一号消息队列监听器: " + data);
    }

    @RabbitListener(queues = "yyds2")
    public void receiver2(String data) {
        System.out.println("二号消息队列监听器: " + data);
    }*/

}
