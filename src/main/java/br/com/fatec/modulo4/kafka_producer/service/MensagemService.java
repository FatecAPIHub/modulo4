package br.com.fatec.modulo4.kafka_producer.service;

import br.com.fatec.modulo4.kafka_producer.model.Mensagem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MensagemService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public void enviarMensagem(String conteudo, String remetente, String prioridade) {
        Mensagem mensagem = new Mensagem(
                UUID.randomUUID().toString(),
                conteudo,
                remetente,
                LocalDateTime.now(),
                prioridade
        );

        log.info("Enviando mensagem: {}", mensagem);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, mensagem.getId(), mensagem);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Mensagem enviada com sucesso! ID: {} | Partição: {} | Offset: {}",
                        mensagem.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Erro ao enviar mensagem: {}", ex.getMessage());
            }
        });
    }
}
