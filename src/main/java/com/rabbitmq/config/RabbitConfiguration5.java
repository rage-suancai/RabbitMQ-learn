/*package com.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration5 { // SpringBoot连接测试(路由模式)

    @Bean("directExchange")
    public Exchange exchange() {
        return ExchangeBuilder.directExchange("amq.direct").build();
    }

    @Bean("yydsQueue")
    public Queue queue() {
        return QueueBuilder.nonDurable("yyds").build();
    }

    @Bean("binding1")
    public Binding binding1(@Qualifier("directExchange") Exchange exchange,
                           @Qualifier("yydsQueue") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("yyds1")
                .noargs();

    }
    @Bean("binding2")
    public Binding binding2(@Qualifier("directExchange") Exchange exchange,
                            @Qualifier("yydsQueue") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("yyds2")
                .noargs();

    }

}*/
