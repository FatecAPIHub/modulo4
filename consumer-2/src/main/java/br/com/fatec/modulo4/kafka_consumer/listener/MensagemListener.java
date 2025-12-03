package br.com.fatec.modulo4.kafka_consumer.listener;


import br.com.fatec.modulo4.kafka_consumer.model.Mensagem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MensagemListener {

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirMensagem(
            @Payload Mensagem mensagem,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓");
        log.info("CONSUMER 2 - Mensagem recebida!");
        log.info("ID: {}", mensagem.getId());
        log.info("Conteúdo: {}", mensagem.getConteudo());
        log.info("Remetente: {}", mensagem.getRemetente());
        log.info("Prioridade: {}", mensagem.getPrioridade());
        log.info("Data/Hora: {}", mensagem.getDataHora());
        log.info("Partição: {} | Offset: {}", partition, offset);
        log.info("▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓");

        // Simula processamento diferente do Consumer 1
        processarMensagem(mensagem);
    }

    private void processarMensagem(Mensagem mensagem) {
        try {
            Thread.sleep(150); // Simula processamento um pouco mais lento
            log.info("CONSUMER 2 - Mensagem processada com sucesso: {}", mensagem.getId());
        } catch (Exception e) {
            log.error("CONSUMER 2 - Erro ao processar mensagem: {}", e.getMessage());
        }
    }
}