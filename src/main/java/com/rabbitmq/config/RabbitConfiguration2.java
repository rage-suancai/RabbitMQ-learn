/*package com.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration2 {

    @Bean("directDlExchange")
    public Exchange dlExchange() {
        return ExchangeBuilder.directExchange("dlx.direct").build();
    }

    @Bean("yydsDlQueue")
    public Queue dlqueue() {

        return QueueBuilder
                .nonDurable("dl-yyds")
                .build();

    }
    @Bean("dlBinding")
    public Binding dlBinding(@Qualifier("directDlExchange") Exchange exchange,
                             @Qualifier("yydsDlQueue") Queue queue) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("dl-yyds")
                .noargs();

    }

    @Bean("yydsQueue")
    public Queue queue() {

        return QueueBuilder
                .nonDurable("yyds")
                .deadLetterExchange("dlx.direct")
                .deadLetterRoutingKey("dl-yyds")
                //.ttl(5000)
                .maxLength(3)
                .build();

    }

    @Bean("jacksonConverter")
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

}*/
