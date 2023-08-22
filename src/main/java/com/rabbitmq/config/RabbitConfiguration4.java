/*package com.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration4 {

    @Bean("fanoutExchange")
    public Exchange exchange() {
        return ExchangeBuilder.fanoutExchange("amq.fanout").build();
    }

    @Bean("yydsQueue1")
    public Queue queue1() {
        return QueueBuilder.nonDurable("yyds1").build();
    }
    @Bean("binding1")
    public Binding binding1(@Qualifier("fanoutExchange") Exchange exchange,
                           @Qualifier("yydsQueue1") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("yyds1")
                .noargs();

    }

    @Bean("yydsQueue2")
    public Queue queue2() {
        return QueueBuilder.nonDurable("yyds2").build();
    }
    @Bean("binding2")
    public Binding binding2(@Qualifier("fanoutExchange") Exchange exchange,
                            @Qualifier("yydsQueue2") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("yyds2")
                .noargs();

    }

}*/
