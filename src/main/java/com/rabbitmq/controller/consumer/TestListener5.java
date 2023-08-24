package com.rabbitmq.controller.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener5 { // SpringBoot连接测试(主题模式)

    /*@RabbitListener(queues = "yyds")
    public void receiver1(String data) {
        System.out.println("一号消息队列监听器: " + data);
    }*/

}
