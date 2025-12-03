#!/bin/bash

# Script de teste para o projeto Kafka
# Facilita a demonstração e testes

echo "======================================"
echo "   SCRIPT DE TESTE - KAFKA PROJECT"
echo "======================================"
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

function print_section() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

function print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

function print_error() {
    echo -e "${RED}✗ $1${NC}"
}

function print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# 1. Verificar se o Kafka está rodando
function check_kafka() {
    print_section "1. Verificando Kafka"

    if docker ps | grep -q kafka-broker-1; then
        print_success "Kafka Broker 1 está rodando"
    else
        print_error "Kafka Broker 1 NÃO está rodando"
        print_info "Execute: docker-compose up -d"
        exit 1
    fi

    if docker ps | grep -q kafka-broker-2; then
        print_success "Kafka Broker 2 está rodando"
    else
        print_error "Kafka Broker 2 NÃO está rodando"
    fi

    if docker ps | grep -q kafka-broker-3; then
        print_success "Kafka Broker 3 está rodando"
    else
        print_error "Kafka Broker 3 NÃO está rodando"
    fi
    echo ""
}

# 2. Verificar tópico
function check_topic() {
    print_section "2. Verificando Tópico"

    docker exec kafka-broker-1 kafka-topics --bootstrap-server localhost:9092 --describe --topic mensagens-projeto
    echo ""
}

# 3. Verificar aplicações
function check_applications() {
    print_section "3. Verificando Aplicações"

    # Producer
    if curl -s http://localhost:8080/api/mensagens/health > /dev/null 2>&1; then
        print_success "Producer (8080) está respondendo"
    else
        print_error "Producer (8080) NÃO está respondendo"
    fi

    # Consumer 1
    if curl -s http://localhost:8081/api/health > /dev/null 2>&1; then
        print_success "Consumer 1 (8081) está respondendo"
    else
        print_error "Consumer 1 (8081) NÃO está respondendo"
    fi

    # Consumer 2
    if curl -s http://localhost:8082/api/health > /dev/null 2>&1; then
        print_success "Consumer 2 (8082) está respondendo"
    else
        print_error "Consumer 2 (8082) NÃO está respondendo"
    fi
    echo ""
}

# 4. Enviar mensagens de teste
function send_test_messages() {
    print_section "4. Enviando Mensagens de Teste"

    print_info "Enviando 5 mensagens..."

    for i in {1..5}; do
        response=$(curl -s -X POST "http://localhost:8080/api/mensagens?conteudo=Mensagem%20Teste%20$i&remetente=ScriptTeste&prioridade=NORMAL")
        if echo $response | grep -q "ENVIADA"; then
            print_success "Mensagem $i enviada"
        else
            print_error "Erro ao enviar mensagem $i"
        fi
        sleep 1
    done
    echo ""
}

# 5. Verificar Consumer Groups
function check_consumer_groups() {
    print_section "5. Consumer Groups"

    print_info "Grupo: grupo-consumer-1"
    docker exec kafka-broker-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group grupo-consumer-1
    echo ""

    print_info "Grupo: grupo-consumer-2"
    docker exec kafka-broker-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group grupo-consumer-2
    echo ""
}

# 6. Teste de stress
function stress_test() {
    print_section "6. Teste de Stress (50 mensagens)"

    print_info "Enviando 50 mensagens rapidamente..."

    for i in {1..50}; do
        curl -s -X POST "http://localhost:8080/api/mensagens?conteudo=Stress%20$i&remetente=StressTest" > /dev/null &
    done

    wait
    print_success "50 mensagens enviadas!"
    print_info "Verifique os logs dos consumidores para ver a distribuição nas partições"
    echo ""
}

# 7. Teste de resiliência
function resilience_test() {
    print_section "7. Teste de Resiliência"

    print_info "Derrubando Broker 2..."
    docker-compose stop kafka-broker-2
    sleep 2

    print_info "Enviando mensagem com broker 2 fora do ar..."
    response=$(curl -s -X POST "http://localhost:8080/api/mensagens?conteudo=Teste%20Resiliencia&remetente=ResilienceTest")

    if echo $response | grep -q "ENVIADA"; then
        print_success "Mensagem enviada com sucesso mesmo com broker fora!"
    else
        print_error "Falha ao enviar mensagem"
    fi

    print_info "Subindo Broker 2 novamente..."
    docker-compose start kafka-broker-2
    sleep 5
    print_success "Broker 2 restaurado"
    echo ""
}

# Menu principal
function show_menu() {
    echo ""
    echo "Escolha uma opção:"
    echo "1) Verificação Completa do Sistema"
    echo "2) Enviar Mensagens de Teste (5 mensagens)"
    echo "3) Teste de Stress (50 mensagens)"
    echo "4) Teste de Resiliência (derrubar/subir broker)"
    echo "5) Ver Consumer Groups"
    echo "6) Ver Tópico"
    echo "7) Executar TODOS os testes"
    echo "0) Sair"
    echo ""
    read -p "Opção: " option

    case $option in
        1)
            check_kafka
            check_topic
            check_applications
            ;;
        2)
            send_test_messages
            ;;
        3)
            stress_test
            ;;
        4)
            resilience_test
            ;;
        5)
            check_consumer_groups
            ;;
        6)
            check_topic
            ;;
        7)
            check_kafka
            check_topic
            check_applications
            send_test_messages
            check_consumer_groups
            stress_test
            print_info "Teste de resiliência pulado (requer confirmação manual)"
            ;;
        0)
            echo "Saindo..."
            exit 0
            ;;
        *)
            print_error "Opção inválida"
            ;;
    esac

    show_menu
}

# Execução
check_kafka
show_menu