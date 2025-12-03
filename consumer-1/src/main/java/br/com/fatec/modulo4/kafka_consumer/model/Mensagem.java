package br.com.fatec.modulo4.kafka_consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mensagem {
    private String id;
    private String conteudo;
    private String remetente;
    private LocalDateTime dataHora;
    private String prioridade;
}

