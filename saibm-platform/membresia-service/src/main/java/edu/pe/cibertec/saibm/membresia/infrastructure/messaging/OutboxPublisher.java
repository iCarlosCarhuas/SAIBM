package edu.pe.cibertec.saibm.membresia.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.pe.cibertec.saibm.membresia.infrastructure.persistence.OutboxJpaRepository;

@Component
@ConditionalOnProperty(name = "membresia.outbox.publisher-enabled", havingValue = "true")
public class OutboxPublisher {
    private final OutboxJpaRepository outbox;
    private final RabbitTemplate rabbit;

    public OutboxPublisher(OutboxJpaRepository outbox, RabbitTemplate rabbit) {
        this.outbox = outbox;
        this.rabbit = rabbit;
    }

    @Scheduled(fixedDelayString = "${membresia.outbox.delay-ms:500}")
    public void publish() {
        for (var event : outbox.pending(PageRequest.of(0, 50))) {
            rabbit.convertAndSend(MembershipRabbitConfig.EXCHANGE, event.getEventType(), event.getPayloadJson());
        }
    }
}
