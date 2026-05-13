package com.learnify.notificationservice.config;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQConfig — NEW FILE (notification-service)
 *
 * The notification-service is the CONSUMER of all 3 event queues:
 *   - learnify.enrollment.queue  → EnrollmentEventListener
 *   - learnify.payment.queue     → PaymentEventListener
 *   - learnify.quiz.queue        → QuizEventListener
 *
 * Queue/Exchange declarations here ensure they are created if they don't
 * exist when the notification-service starts first.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE           = "learnify.events";
    public static final String ENROLLMENT_QUEUE   = "learnify.enrollment.queue";
    public static final String PAYMENT_QUEUE      = "learnify.payment.queue";
    public static final String QUIZ_QUEUE         = "learnify.quiz.queue";
    public static final String ROUTING_ENROLLMENT = "enrollment.created";
    public static final String ROUTING_PAYMENT    = "payment.success";
    public static final String ROUTING_QUIZ       = "quiz.submitted";

    @Bean
    public TopicExchange learnifyExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

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

    @Bean
    public Binding enrollmentBinding(Queue enrollmentQueue, TopicExchange learnifyExchange) {
        return BindingBuilder.bind(enrollmentQueue).to(learnifyExchange).with(ROUTING_ENROLLMENT);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange learnifyExchange) {
        return BindingBuilder.bind(paymentQueue).to(learnifyExchange).with(ROUTING_PAYMENT);
    }

    @Bean
    public Binding quizBinding(Queue quizQueue, TopicExchange learnifyExchange) {
        return BindingBuilder.bind(quizQueue).to(learnifyExchange).with(ROUTING_QUIZ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
