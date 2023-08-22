package com.rabbitmq.controller.consumer;

import com.rabbitmq.entity.User;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener1 { // SpringBoot连接测试(生产者消费者)

    /*@RabbitListener(queues = "yyds")
    public void test(Message message) {
        System.out.println(new String(message.getBody()));
    }*/

    /*@RabbitListener(queues = "yyds")
    public String receiver(String data) {

        System.out.println("一号消息队列监听器 " + data);
        return "收到";

    }*/

    /*@RabbitListener(queues = "yyds", messageConverter = "jacksonConverter")
    public void receiver(User user) {
        System.out.println(user);
    }*/

    /*@RabbitListener(queues = "dl-yyds", messageConverter = "jacksonConverter")
    public void receiver(User user) {
        System.out.println(user);
    }*/

}
