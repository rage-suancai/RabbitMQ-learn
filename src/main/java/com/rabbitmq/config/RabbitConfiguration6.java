/*package com.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration6 { // SpringBoot连接测试(主题模式)

    @Bean("topicExchange")
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange("amq.topic").build();
    }

    @Bean("yydsQueue")
    public Queue queue() {
        return QueueBuilder.nonDurable("yyds").build();
    }

    @Bean("binding")
    public Binding binding(@Qualifier("topicExchange") Exchange exchange,
                           @Qualifier("yydsQueue") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("*.test.*")
                .noargs();

    }

}*/
