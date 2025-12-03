package br.com.fatec.modulo4.kafka_producer.controller;

import br.com.fatec.modulo4.kafka_producer.service.MensagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mensagens")
@RequiredArgsConstructor
public class MensagemController {

    private final MensagemService mensagemService;

    @PostMapping
    public ResponseEntity<Map<String, String>> enviarMensagem(
            @RequestParam @NotBlank String conteudo,
            @RequestParam @NotBlank String remetente,
            @RequestParam(defaultValue = "NORMAL") String prioridade) {

        log.info("Recebida requisição para enviar mensagem: conteudo={}, remetente={}", conteudo, remetente);

        mensagemService.enviarMensagem(conteudo, remetente, prioridade);

        return ResponseEntity.ok(Map.of(
                "status", "ENVIADA",
                "mensagem", "Mensagem publicada no Kafka com sucesso"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Producer"));
    }
}