# 📚 Online Course System API

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?style=flat&logo=kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?style=flat&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?style=flat&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containers-blue?style=flat&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

> RESTful API para gerenciamento de cursos online, alunos e matrículas com controle de acesso baseado em perfis (RBAC),
> autenticação JWT e documentação Swagger/OpenAPI.

---

## 📌 Sumário

- [Recursos da API](#-recursos-da-api)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura e Segurança](#-arquitetura-e-segurança)
- [Como Executar o Projeto](#-como-executar-o-projeto)
- [Documentação da API (Swagger)](#-documentação-da-api-swagger)
- [Suíte de Testes](#-suíte-de-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)

---

## 🚀 Recursos da API

* **Autenticação & Segurança:** Cadastro de usuários, login com emissão de token JWT e controle de permissões por perfil
  (`ROLE_USER` e `ROLE_ADMIN`).
* **Gerenciamento de Alunos:** CRUD completo de estudantes, busca paginada com filtros por nome/status e histórico de
  cursos vinculados.
* **Gerenciamento de Cursos:** CRUD completo de cursos e consulta de alunos matriculados por curso.
* **Matrículas & Regras de Negócio:** Matrícula de alunos em cursos, cancelamento de matrícula e validação de
  duplicidade.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Kotlin
* **Framework:** Spring Boot 3
* **Segurança:** Spring Security + JWT (JSON Web Token)
* **Persistência de Dados:** Spring Data JPA + Hibernate
* **Banco de Dados:** PostgreSQL (Produção/Docker) & H2 Database (Ambiente de Testes)
* **Documentação:** Springdoc OpenAPI / Swagger 3
* **Conteinerização:** Docker & Docker Compose (Multi-stage build)
* **CI/CD:** GitHub Actions

---

## 🔐 Arquitetura e Segurança (RBAC)

A API utiliza o modelo **RBAC (Role-Based Access Control)** para proteger seus recursos:

| Role             | Permissões                                                                                |
|:-----------------|:------------------------------------------------------------------------------------------|
| **`ROLE_USER`**  | Pode realizar autenticação e operações de **leitura** (`GET`) em cursos e estudantes.     |
| **`ROLE_ADMIN`** | Acesso total: leitura, criação (`POST`), atualização (`PUT/PATCH`) e exclusão (`DELETE`). |

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos

* [Docker](https://www.docker.com/) e **Docker Compose** instalados.

### 1. Clonar o Repositório

```bash
git clone [https://github.com/seu-usuario/online-course-system.git](https://github.com/seu-usuario/online-course-system.git)
cd online-course-system



### 2. Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto baseado no exemplo:

```env
POSTGRES_DB=courses
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT_SECRET=meu_secret_super_seguro_e_longo_para_jwt_12345

```

### 3. Subir o Ambiente com Docker Compose

```bash
docker compose up --build

```

A API estará acessível em `http://localhost:8080`.

---

## 📖 Documentação da API (Swagger)

A documentação interativa da API está disponível via Swagger UI. Através dela, é possível testar os endpoints e enviar o
token JWT usando o botão **Authorize**.

* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Spec (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🧪 Suíte de Testes

O projeto conta com uma cobertura robusta de testes automatizados incluindo testes unitários, testes de controller
(MockMvc) e testes End-to-End (E2E) para validação do fluxo de segurança e RBAC.

Para rodar a suíte completa de testes via terminal:

```bash
./gradlew test

```

O relatório visual de cobertura em HTML será gerado em: `build/reports/tests/test/index.html`.

---

## 📂 Estrutura do Projeto

```text
src/
├── main/
│   └── kotlin/
│       └── com/janapc/online_course_system/
│           ├── auth/         # Autenticação, Usuários e Segurança JWT
│           ├── student/      # Domínio de Alunos
│           ├── course/       # Domínio de Cursos
│           ├── enrollment/   # Domínio de Matrículas
│           └── common/       # Configurações globais (Swagger, Exceptions)
└── test/                     # Testes Unitários, Controllers e E2E

```

---

## 📝 Licença

Este projeto está sob a licença [MIT](https://www.google.com/search?q=LICENSE).
