package com.learnify.paymentservice.event;
import com.learnify.paymentservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * PaymentEventPublisher — NEW FILE
 *
 * Publishes PAYMENT_SUCCESS events after a payment or subscription
 * is successfully processed and saved to the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a PaymentSuccessEvent to the learnify exchange.
     * Called from PaymentServiceImpl.processPayment() and subscribe().
     */
    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_PAYMENT,
                    event
            );
            log.info("[PaymentPublisher] Published PAYMENT_SUCCESS — " +
                            "paymentId={} studentId={} amount={} type={}",
                    event.getPaymentId(), event.getStudentId(),
                    event.getAmount(), event.getPaymentType());
        } catch (Exception e) {
            // Never fail the payment because notification failed
            log.error("[PaymentPublisher] Failed to publish PAYMENT_SUCCESS: {}",
                    e.getMessage(), e);
        }
    }
}
