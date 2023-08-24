/*package com.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class RabbitConfiguration3 { // SpringBoot连接测试(工作队列)

    @Resource
    private CachingConnectionFactory connectionFactory;

    @Bean("directExchange")
    public Exchange exchange() {
        return ExchangeBuilder.directExchange("amp.direct").build();
    }

    @Bean("yydsQueue")
    public Queue queue() {

        return QueueBuilder
                .nonDurable("yyds")
                .build();

    }
    @Bean("binding")
    public Binding binding(@Qualifier("directExchange") Exchange exchange,
                           @Qualifier("yydsQueue") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("my-yyds")
                .noargs();

    }

    @Bean(name = "listenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer() {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);
        return factory;

    }

}*/

