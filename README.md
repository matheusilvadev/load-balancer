# ⚖️ Layer 7 Load Balancer em Java

Uma implementação de **Balanceador de Carga (Load Balancer) na Camada de Aplicação (Layer 7)** construída em Java Puro (sem frameworks pesados), utilizando o algoritmo **Round Robin** (com disponibilidade para outros algoritmos) de forma atômica e *thread-safe* para distribuição de requisições HTTP entre servidores backend.

---

## 📖 Visão Geral

Este projeto atua como um **Proxy Reverso na Camada 7 do modelo OSI**, recebendo requisições HTTP de clientes e interceptando seus cabeçalhos para roteá-las de maneira circular e distribuída para uma lista de servidores backend ativos.

Diferente de um balanceador na Camada 4 (Transporte), que opera apenas com pacotes TCP/UDP, o balanceador Layer 7 analisa e preserva os metadados da requisição HTTP (como cabeçalhos `X-Forwarded`), garantindo rastreabilidade do cliente original.

---

## ✨ Funcionalidades

- **Roteamento Layer 7 (HTTP):** Interceptação, encaminhamento de requisições e repasse da resposta do backend ao cliente.
- **Algoritmo Round Robin Lock-Free:** Distribuição equitativa de carga utilizando `AtomicInteger` e operador de módulo `%`, sem bloqueios de *threads* (`synchronized`).
- **Preservação de Cabeçalhos HTTP:** Injeção automática dos cabeçalhos de rastreabilidade:
  - `X-Forwarded-For`: IP original do cliente.
  - `X-Forwarded-Proto`: Protocolo utilizado (`http` / `https`).
  - `X-Forwarded-Host`: Host original solicitado.
- **Tratamento de Falhas (Resiliência):** Resposta HTTP `503 Service Unavailable` caso nenhum backend esteja disponível.
- **Mock Backend Server:** Servidor dummy leve integrado para simulação e validação visual das requisições.
- **Suíte de Testes de Integração:** Validação automatizada do balanceamento em tempo de execução.

---

## 🏗 Arquitetura

O projeto foi desenhado seguindo os princípios de responsabilidade única e desacoplamento de componentes:

              +-----------------------+
              |    Cliente HTTP       |
              +-----------+-----------+
                          |
                 GET /    | (Porta 8080)
                          v
              +-----------------------+
              |     Load Balancer     |
              |     (ProxyHandler)    |
              +-----------+-----------+
                          |
    +----------------------+----------------------+
    | Escolhe Backend (RoundRobinRoutingStrategy) |
    +----------------------+----------------------+
                          |
         +----------------+----------------+
         |                                 |
         v                                 v
         +------------------------+        +------------------------+
         |   Backend 1 (Port 8081) |       |   Backend 2 (Port 8082) |
         +------------------------+        +------------------------+
---

### Principais Componentes:

1. **`BackendRegistry`**: Mantém a lista de URLs dos servidores backend cadastrados.
2. **`RoutingStrategy` / `RoundRobinRoutingStrategy`**: Interface e implementação responsável por selecionar o próximo servidor backend disponível.
3. **`HttpProxyClient`**: Cliente HTTP de alto desempenho (`java.net.http.HttpClient`) responsável por retransmitir a chamada ao destino final e capturar o retorno.
4. **`ProxyHandler`**: Interceptador do servidor web (`com.sun.net.httpserver.HttpHandler`) que coordena a rota, executa a proxyficação e retorna o resultado ao cliente.

---

## 🧮 Algoritmo Round Robin & Thread-Safety

Para garantir alta concorrência em ambientes *multithread* sem sacrificar o desempenho:

- **Contador Atômico (`AtomicInteger`):** Incrementa o índice global de requisições utilizando instruções nativas da CPU (*Compare-And-Swap / CAS*), evitando *locks* pessimistas (`synchronized`).
- **Aritmética Modular (`% N`):** Garante a navegação circular sobre a lista de $N$ servidores:


*(O operador bitwise `& Integer.MAX_VALUE` é utilizado para evitar índices negativos em caso de overflow do contador).*

---

## 🛠 Tecnologias Utilizadas

* **Linguagem:** Java 17
* **HTTP Server Embutido:** `com.sun.net.httpserver.HttpServer`
* **HTTP Client:** `java.net.http.HttpClient`
* **Testes Automatizados:** JUnit 5 & Mockito
* **Gerenciador de Dependências:** Maven

---
