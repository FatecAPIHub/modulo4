# Projeto Kafka - Spring Boot 3 + Java 21

## 📋 Descrição do Projeto

Sistema de mensageria distribuída utilizando Apache Kafka com:
- **1 Produtor** que envia mensagens para o Kafka
- **2 Consumidores** que recebem todas as mensagens (com Group IDs diferentes)
- **3 Brokers** Kafka para garantir resiliência
- **5 Partições** para paralelismo e redundância

---

## 🏗️ Arquitetura

```
                  ┌─────────────────┐
                  │   Producer      │
                  │   (Port 8080)   │
                  └────────┬────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │    Kafka Cluster       │
              │  ┌──────────────────┐  │
              │  │ Broker 1 (9092)  │  │
              │  │ Broker 2 (9093)  │  │
              │  │ Broker 3 (9094)  │  │
              │  └──────────────────┘  │
              │  Topic: mensagens-projeto│
              │  Partições: 5          │
              │  Replicação: 3         │
              └──────────┬─────────────┘
                         │
          ┌──────────────┴──────────────┐
          ▼                             ▼
    ┌──────────┐                ┌──────────┐
    │Consumer 1│                │Consumer 2│
    │Port 8081 │                │Port 8082 │
    │Group: 1  │                │Group: 2  │
    └──────────┘                └──────────┘
```

---

## 🔑 Conceitos Importantes

### Group ID
- **Grupos DIFERENTES**: Cada consumidor recebe TODAS as mensagens
- **Mesmo grupo**: Kafka distribui mensagens entre os consumidores (load balancing)
- Neste projeto: `grupo-consumer-1` e `grupo-consumer-2`

### Partições (5)
- Permitem paralelismo na leitura
- Melhor distribuição de carga
- Cada mensagem vai para uma partição baseada na chave

### Replicação (3 brokers)
- Fator de replicação: 3
- Garante disponibilidade mesmo se um broker cair
- `acks=all`: Garante que todos os brokers confirmem o recebimento

---

## 📦 Estrutura do Projeto

```
projeto-kafka/
├── docker-compose.yml          # Infraestrutura Kafka
├── producer/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/projeto/faculdade/producer/
│       │   ├── ProducerApplication.java
│       │   ├── config/KafkaProducerConfig.java
│       │   ├── model/Mensagem.java
│       │   ├── service/MensagemService.java
│       │   └── controller/MensagemController.java
│       └── resources/application.yml
├── consumer-1/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/projeto/faculdade/consumer1/
│       │   ├── Consumer1Application.java
│       │   ├── config/KafkaConsumerConfig.java
│       │   ├── model/Mensagem.java
│       │   ├── listener/MensagemListener.java
│       │   └── controller/HealthController.java
│       └── resources/application.yml
└── consumer-2/
    ├── pom.xml
    └── src/main/
        ├── java/com/projeto/faculdade/consumer2/
        │   ├── Consumer2Application.java
        │   ├── config/KafkaConsumerConfig.java
        │   ├── model/Mensagem.java
        │   ├── listener/MensagemListener.java
        │   └── controller/HealthController.java
        └── resources/application.yml
```

---

## 🚀 Como Executar

### 1. Pré-requisitos
- Docker e Docker Compose instalados
- Java 21 instalado
- Maven 3.6+ instalado

### 2. Subir a Infraestrutura Kafka

```bash
# No diretório raiz do projeto
docker-compose up -d

# Verificar se os containers subiram
docker-compose ps

# Verificar logs
docker-compose logs -f kafka-broker-1
```

### 3. Verificar o Tópico Criado

```bash
# Listar tópicos
docker exec -it kafka-broker-1 kafka-topics --bootstrap-server localhost:9092 --list

# Ver detalhes do tópico
docker exec -it kafka-broker-1 kafka-topics --bootstrap-server localhost:9092 --describe --topic mensagens-projeto
```

**Saída esperada:**
```
Topic: mensagens-projeto
PartitionCount: 5
ReplicationFactor: 3
```

### 4. Executar as Aplicações

**Terminal 1 - Producer:**
```bash
cd producer
mvn clean install
mvn spring-boot:run
```

**Terminal 2 - Consumer 1:**
```bash
cd consumer-1
mvn clean install
mvn spring-boot:run
```

**Terminal 3 - Consumer 2:**
```bash
cd consumer-2
mvn clean install
mvn spring-boot:run
```

---

## 🧪 Testando o Sistema

### 1. Verificar Health das Aplicações

```bash
# Producer
curl http://localhost:8080/api/mensagens/health

# Consumer 1
curl http://localhost:8081/api/health

# Consumer 2
curl http://localhost:8082/api/health
```

### 2. Enviar Mensagens

```bash
# Enviar uma mensagem
curl -X POST "http://localhost:8080/api/mensagens?conteudo=Ola Kafka&remetente=Aluno&prioridade=ALTA"

# Enviar várias mensagens
curl -X POST "http://localhost:8080/api/mensagens?conteudo=Mensagem 1&remetente=Sistema"
curl -X POST "http://localhost:8080/api/mensagens?conteudo=Mensagem 2&remetente=Sistema"
curl -X POST "http://localhost:8080/api/mensagens?conteudo=Mensagem 3&remetente=Sistema"
```

### 3. Observar os Logs

Você verá nos logs de **ambos** os consumidores cada mensagem sendo processada:

**Consumer 1:**
```
═══════════════════════════════════════════════════════════
CONSUMER 1 - Mensagem recebida!
ID: abc123...
Conteúdo: Ola Kafka
Remetente: Aluno
Partição: 2 | Offset: 0
═══════════════════════════════════════════════════════════
```

**Consumer 2:**
```
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
CONSUMER 2 - Mensagem recebida!
ID: abc123...
Conteúdo: Ola Kafka
Remetente: Aluno
Partição: 2 | Offset: 0
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
```

---

## 🔍 Monitoramento Kafka

### Verificar Mensagens no Tópico

```bash
# Consumir mensagens do início
docker exec -it kafka-broker-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic mensagens-projeto \
  --from-beginning
```

### Verificar Consumer Groups

```bash
# Listar grupos
docker exec -it kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Ver detalhes de um grupo
docker exec -it kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group grupo-consumer-1
```

---

## 🧩 Componentes Principais

### Producer

**Características:**
- Envia mensagens em JSON
- `acks=all` para garantir durabilidade
- Idempotência habilitada
- Retries configurados

**Endpoint:**
```
POST http://localhost:8080/api/mensagens
Params: conteudo, remetente, prioridade
```

### Consumers

**Consumer 1:**
- Group ID: `grupo-consumer-1`
- Porta: 8081
- Concurrency: 3 threads

**Consumer 2:**
- Group ID: `grupo-consumer-2`
- Porta: 8082
- Concurrency: 3 threads

**Configurações importantes:**
- `auto.offset.reset=earliest`: Consome desde o início
- `enable.auto.commit=true`: Commit automático
- JSON deserialização habilitada

---

## 📊 Testando Resiliência

### Derrubar um Broker

```bash
# Parar broker 2
docker-compose stop kafka-broker-2

# Enviar mensagens - deve continuar funcionando
curl -X POST "http://localhost:8080/api/mensagens?conteudo=Teste Resiliencia&remetente=Sistema"

# Subir novamente
docker-compose start kafka-broker-2
```

### Testar Paralelismo

Envie múltiplas mensagens rapidamente e observe a distribuição nas 5 partições:

```bash
for i in {1..20}; do
  curl -X POST "http://localhost:8080/api/mensagens?conteudo=Mensagem $i&remetente=Sistema"
done
```

---

## 🎯 Pontos Importantes para Apresentação

1. **Group IDs Diferentes**: Explique que isso permite broadcast (todos recebem)
2. **3 Brokers**: Demonstre a resiliência derrubando um broker
3. **5 Partições**: Mostre no log como mensagens são distribuídas
4. **Replicação**: Explique o fator de replicação 3
5. **Configurações de Resiliência**: `acks=all`, `idempotence`, `retries`


---

## 📚 Conceitos Avaliados

✅ **Kafka Producer configurado corretamente**  
✅ **2 Consumers com Groups IDs diferentes**  
✅ **Mensagens chegam em AMBOS os consumidores**  
✅ **Cluster com 3 brokers para resiliência**  
✅ **Tópico com 5 partições**  
✅ **Fator de replicação 3**  
✅ **Configurações de resiliência e performance**
