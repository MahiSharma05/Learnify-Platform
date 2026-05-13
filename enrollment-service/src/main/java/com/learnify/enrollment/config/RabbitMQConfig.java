package com.learnify.enrollment.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {
    // ── Exchange ─────────────────────────────────────────────────────────────
    public static final String EXCHANGE = "learnify.events";

    // ── Queue Names ───────────────────────────────────────────────────────────
    public static final String ENROLLMENT_QUEUE  = "learnify.enrollment.queue";
    public static final String PAYMENT_QUEUE     = "learnify.payment.queue";
    public static final String QUIZ_QUEUE        = "learnify.quiz.queue";

    // ── Routing Keys ──────────────────────────────────────────────────────────
    public static final String ROUTING_ENROLLMENT = "enrollment.created";
    public static final String ROUTING_PAYMENT    = "payment.success";
    public static final String ROUTING_QUIZ       = "quiz.submitted";

    // ── Exchange Declaration ──────────────────────────────────────────────────
    @Bean
    public TopicExchange learnifyExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    // ── Queue Declarations ────────────────────────────────────────────────────
    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder.durable(ENROLLMENT_QUEUE).build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE).build();
    }

    @Bean
    public Queue quizQueue() {
        return QueueBuilder.durable(QUIZ_QUEUE).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────
    @Bean
    public Binding enrollmentBinding(Queue enrollmentQueue, TopicExchange learnifyExchange) {
        return BindingBuilder
                .bind(enrollmentQueue)
                .to(learnifyExchange)
                .with(ROUTING_ENROLLMENT);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange learnifyExchange) {
        return BindingBuilder
                .bind(paymentQueue)
                .to(learnifyExchange)
                .with(ROUTING_PAYMENT);
    }

    @Bean
    public Binding quizBinding(Queue quizQueue, TopicExchange learnifyExchange) {
        return BindingBuilder
                .bind(quizQueue)
                .to(learnifyExchange)
                .with(ROUTING_QUIZ);
    }

    // ── Message Converter (JSON) ──────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── RabbitTemplate with JSON converter ───────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
