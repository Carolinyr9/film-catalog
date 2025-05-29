# 🎬 Film Catalog API

## 📌 Objetivo e público-alvo da API

Esta API foi desenvolvida para fornecer uma plataforma de gerenciamento de filmes, com funcionalidades de autenticação, cadastro, avaliação e recomendação. O público-alvo inclui:

- Desenvolvedores backend e frontend que desejam integrar funcionalidades de catálogo e recomendação de filmes.
- Equipes de QA que precisam testar funcionalidades REST com autenticação JWT.
- Usuários administrativos responsáveis pelo controle de acesso e curadoria dos dados.

---

## ⚙️ Funcionalidades implementadas

As funcionalidades foram desenvolvidas com base em histórias de usuário, incluindo:

- ✅ **Cadastro de usuários e autenticação via JWT**
- ✅ **Login com autenticação de senha segura**
- ✅ **Cadastro, edição, listagem e exclusão de filmes**
- ✅ **Avaliação de filmes por usuários**
- ✅ **Listagem de destaques e rankings**
- ✅ **Geração de recomendações personalizadas**
- ✅ **Controle de acesso com base em papéis (ADMIN, USER)**

---

## 🚀 Instruções de execução local

### ✅ Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL (ou outro banco compatível configurado no `application.properties`)

### 🔧 Build

```bash
./mvnw clean package
````

### ▶️ Run

./mvnw spring-boot:run


Ou execute o .jar:

java -jar target/film-catalog-0.0.1-SNAPSHOT.jar


## 🔐 Como obter o token JWT e testar os endpoints

### 1. Obtenha um token

Envie uma requisição POST para:

POST /api/auth

Corpo da requisição:

{
  "username": "admin",
  "password": "AdminPassword123!"
}


Resposta:

"eyJhbGciOiJIUzI1NiIsInR..."


### 2. Use o token nos demais endpoints

Adicione no header das requisições:

Authorization: Bearer SEU_TOKEN_JWT

Você pode testar com Postman, Insomnia ou Swagger.


## 🗂️ Resumo do modelo de dados e regras de validação

### 🧑‍💻 Usuário

| Campo      | Descrição                      | Validação                          |
|------------|--------------------------------|-------------------------------------|
| `username` | Nome de usuário                | Obrigatório, único                 |
| `email`    | Endereço de e-mail             | Obrigatório, formato válido, único |
| `password` | Senha do usuário               | Obrigatório, armazenada com BCrypt |
| `roles`    | Perfis de acesso do usuário    | Um ou mais (ex: `ADMIN`, `USER`)   |

---

### 🎞️ Filme

| Campo         | Descrição               | Validação                          |
|---------------|-------------------------|-------------------------------------|
| `title`       | Título do filme         | Obrigatório, único                 |
| `releaseYear` | Ano de lançamento       | Obrigatório, inteiro (ex: 2024)    |
| `genre`       | Gênero do filme         | Enum ou string padronizada         |

### Avaliações

* Só podem ser feitas por usuários autenticados
* Um usuário só pode avaliar um filme uma vez

## 🔐 Autenticação e Autorização

* **JWT**: Gerado após autenticação válida, com expiração e assinatura segura.
* **Spring Security**: Gerencia login, logout, autenticação e controle de rotas.
* **Papéis (roles)**:

  * `ROLE_ADMIN`: acesso total ao sistema
  * `ROLE_USER`: acesso restrito (sem edição de filmes, por exemplo)

As rotas são protegidas por filtros e regras declaradas na configuração de segurança.

## 🧪 Testes implementados

### ✅ Testes Unitários

* Serviços como `UserService`, `FilmService` e `JwtService`
* Validação de lógica de negócio independente do contexto web

### ✅ Testes Funcionais (integração)

* Requisições REST simuladas com autenticação
* Testes para endpoints protegidos e públicos
* Verificação de comportamento esperado com Spring Boot Test

Os testes estão localizados em `src/test/java`.

📬 Em caso de dúvidas ou sugestões, fique à vontade para abrir uma *issue* ou enviar um *pull request*!
