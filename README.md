# 📄 Resume Review Backend

> **API REST que usa Inteligência Artificial para analisar e dar feedback em currículos — construída com Java, Spring Boot e OpenAI.**

---

## 🚀 Sobre o Projeto

O **Resume Review Backend** é o coração de uma aplicação fullstack que automatiza a revisão de currículos com IA. O sistema recebe um currículo, envia o conteúdo para a API da OpenAI e retorna uma análise detalhada com pontos fortes, pontos de melhoria e sugestões práticas para o candidato.

Este projeto demonstra integração real com LLMs em uma arquitetura RESTful robusta — algo cada vez mais valorizado no mercado.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Descrição |
|---|---|
| ☕ Java | Linguagem principal |
| 🌱 Spring Boot | Framework web e injeção de dependência |
| 🐘 PostgreSQL | Banco de dados relacional |
| 🤖 OpenAI API | Motor de análise com IA |
| 🔧 Maven | Gerenciamento de dependências |
| 🗄️ Spring Data JPA | Persistência de dados |

---

## ⚙️ Como Rodar o Projeto

### Pré-requisitos

- Java 17+
- Maven
- PostgreSQL rodando localmente
- Chave de API da OpenAI

### 1. Clone o repositório

```bash
git clone https://github.com/jpadedg/ResumeReviewBackend.git
cd ResumeReviewBackend
```

### 2. Crie o arquivo de configuração

Crie o arquivo `src/main/resources/application.properties` com o seguinte conteúdo:

```properties
spring.application.name=first-web-api

spring.datasource.url=[url-do-BD]
spring.datasource.username=[username]
spring.datasource.password=[password]

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

openai.api.key=[CHAVE-API-OPENAI]
```

> ⚠️ **Substitua** `[url-do-BD]`, `[username]`, `[password]` e `[CHAVE-API-OPENAI]` pelos seus valores reais.  
> ⚠️ **Nunca** faça commit deste arquivo com credenciais reais. Ele já está no `.gitignore`.

### 3. Rode a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 📡 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/resumes/{userId}` | Envia o currículo de um usuário para análise pela IA |
| `POST` | `/resumes/{userId}/job` | Envia o currículo junto com uma vaga específica para análise direcionada |

---

## 🧠 Como Funciona

```
Cliente → POST /review (texto do currículo)
              ↓
         Spring Boot recebe e valida
              ↓
         Envia para OpenAI API
              ↓
         IA retorna análise estruturada
              ↓
         Salva no PostgreSQL
              ↓
         Retorna feedback ao cliente
```

---

## 📁 Estrutura do Projeto

```
src/
└── main/
    ├── java/
    │   └── com/example/
    │       ├── controller/   # Camada de entrada HTTP
    │       ├── service/      # Lógica de negócio e integração com IA
    │       ├── model/        # Entidades JPA
    │       └── repository/   # Acesso ao banco de dados
    └── resources/
        └── application.properties  # ⚠️ Criado localmente (não versionado)
```

---

## 🔒 Segurança

- Credenciais e chaves de API são mantidas **fora do repositório** via `application.properties` (listado no `.gitignore`)
- Recomenda-se o uso de variáveis de ambiente em ambientes de produção

---


## 👨‍💻 Autor

Feito por **[João Pedro](https://github.com/jpadedg)**  
Entre em contato pelo LinkedIn ou abra uma Issue 🙂
