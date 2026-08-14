package edu.pe.cibertec.saibm.usuario.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.pe.cibertec.saibm.usuario.infrastructure.persistence.UserOutboxRepository;

@Component
@ConditionalOnProperty(name = "usuario.outbox.publisher-enabled", havingValue = "true")
public class UserOutboxPublisher {
    private final UserOutboxRepository outbox;
    private final RabbitTemplate rabbit;

    public UserOutboxPublisher(UserOutboxRepository outbox, RabbitTemplate rabbit) {
        this.outbox = outbox;
        this.rabbit = rabbit;
    }

    @Scheduled(fixedDelayString = "${usuario.outbox.delay-ms:500}")
    public void publish() {
        for (var event : outbox.pending(PageRequest.of(0, 50))) {
            rabbit.convertAndSend(UserRabbitConfig.EXCHANGE, event.eventType(), event.payloadJson());
        }
    }
}
