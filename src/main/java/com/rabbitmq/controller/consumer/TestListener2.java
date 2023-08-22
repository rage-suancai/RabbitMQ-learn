package com.rabbitmq.controller.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;

@Component
public class TestListener2 { // SpringBoot连接测试(工作队列)

    /*@RabbitListener(queues = "yyds", containerFactory = "listenerContainer")
    public void receiver1(String data) {
        System.out.println("一号消息队列监听器: " + data);
    }

    @RabbitListener(queues = "yyds", containerFactory = "listenerContainer")
    public void receiver2(String data) {
        System.out.println("二号消息队列监听器: " + data);
    }*/

    /*@RabbitListener(queues = "yyds", containerFactory = "listenerContainer", concurrency = "10")
    public void receiverList(String data) {
        System.out.println("一号消息队列监听器: " + data);
    }*/

}
