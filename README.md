# 📊 Monitor AIOps - Arquitetura de Observabilidade Inteligente

Uma solução completa e desacoplada de AIOps (Artificial Intelligence for IT Operations) para monitorização de latência em tempo real, deteção de anomalias com Machine Learning e alertas automatizados.

## 🎯 Visão Geral do Projeto

Este projeto foi desenvolvido para demonstrar a orquestração de microsserviços e processamento de dados assíncrono. O sistema monitoriza continuamente a latência de qualquer URL alvo, utiliza algoritmos de Machine Learning para definir o "comportamento normal" (baseline) da rede e alerta sobre degradações de performance antes que se tornem indisponibilidades críticas (downtime).

## 🏗️ Arquitetura e Tecnologias (Tech Stack)

A arquitetura foi desenhada com foco em escalabilidade e separação de responsabilidades (Decoupled Architecture):

* **Coleta e Inteligência (Python):** Um agente local que faz requisições HTTP para o alvo. Utiliza **Scikit-Learn (Isolation Forest)** para treinar um modelo não supervisionado nos primeiros segundos de execução, avaliando se a latência atual é `OK` ou `CRÍTICA`.
* **Mensageria Assíncrona (RabbitMQ):** Atua como um *Message Broker* para garantir que picos de métricas não sobrecarreguem o backend. Desacopla o sensor em Python da aplicação de processamento.
* **Processamento e Regras de Negócio (Java & Spring Boot):** O Worker consome a fila do RabbitMQ em milissegundos, valida a métrica e persiste a informação. Caso a IA sinalize risco crítico, o Spring Boot aciona o serviço de **Notificação por E-mail (SMTP)**.
* **Persistência (PostgreSQL):** Base de dados relacional que armazena a série temporal (Time-Series) das métricas de rede e vereditos do modelo.
* **Observabilidade (Grafana):** Dashboard em tempo real conectado diretamente ao banco, exibindo gráficos de séries temporais para a latência e painéis de Status (Semáforo) com a última classificação da IA.
* **Infraestrutura (Docker & Docker Compose):** Conteinerização do banco de dados, broker de mensageria e painel de visualização para fácil provisionamento.

## 🚀 Fluxo de Dados (Pipeline)

1.  `agent.py` avalia a resposta HTTP da URL alvo.
2.  O modelo de Machine Learning classifica o risco (OK/CRÍTICO).
3.  O payload JSON é publicado na fila `metricas_fila` do RabbitMQ.
4.  O `MetricConsumer` (Java) consome a mensagem e salva no PostgreSQL.
5.  O Grafana consulta o banco e atualiza o NOC (Network Operations Center) a cada 5 segundos.
6.  Em caso de anomalia crítica, um alerta é disparado via e-mail.

## ⚙️ Como Executar o Projeto

### Pré-requisitos
* Docker e Docker Compose
* Python 3.10+
* Java 17+ e Maven

### 1. Subir a Infraestrutura (Docker)
Na raiz do projeto, inicie os contêineres do banco de dados, broker e dashboard:
```bash
docker-compose up -d
