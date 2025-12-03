package br.com.fatec.modulo4.kafka_consumer.listener;

import br.com.fatec.modulo4.kafka_consumer.model.Mensagem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MensagemListener {

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirMensagem(
            @Payload Map<String, Object> mensagemMap,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            Mensagem mensagem = mapToMensagem(mensagemMap);

            log.info("═══════════════════════════════════════════════════════════");
            log.info("CONSUMER 1 - Mensagem recebida!");
            log.info("ID: {}", mensagem.getId());
            log.info("Conteúdo: {}", mensagem.getConteudo());
            log.info("Remetente: {}", mensagem.getRemetente());
            log.info("Prioridade: {}", mensagem.getPrioridade());
            log.info("Data/Hora: {}", mensagem.getDataHora());
            log.info("Partição: {} | Offset: {}", partition, offset);
            log.info("═══════════════════════════════════════════════════════════");
            processarMensagem(mensagem);
        } catch (Exception e) {
            log.error("CONSUMER 1 - Erro ao processar mensagem: {}", e.getMessage(), e);
        }
    }

    private Mensagem mapToMensagem(Map<String, Object> map) {
        Mensagem mensagem = new Mensagem();
        mensagem.setId((String) map.get("id"));
        mensagem.setConteudo((String) map.get("conteudo"));
        mensagem.setRemetente((String) map.get("remetente"));
        mensagem.setPrioridade((String) map.get("prioridade"));

        // Converter array de LocalDateTime
        if (map.get("dataHora") instanceof List) {
            List<Integer> dateTime = (List<Integer>) map.get("dataHora");
            mensagem.setDataHora(LocalDateTime.of(
                    dateTime.get(0), // ano
                    dateTime.get(1), // mês
                    dateTime.get(2), // dia
                    dateTime.get(3), // hora
                    dateTime.get(4), // minuto
                    dateTime.get(5), // segundo
                    dateTime.size() > 6 ? dateTime.get(6) : 0 // nanosegundo
            ));
        }

        return mensagem;
    }

    private void processarMensagem(Mensagem mensagem) {
        try {
            Thread.sleep(100);
            log.info("CONSUMER 1 - Mensagem processada com sucesso: {}", mensagem.getId());
        } catch (Exception e) {
            log.error("CONSUMER 1 - Erro ao processar mensagem: {}", e.getMessage());
        }
    }
}