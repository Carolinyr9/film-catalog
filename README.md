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

Neste projeto, foram desenvolvidos testes automatizados para garantir o correto funcionamento das principais funcionalidades da API, divididos entre testes unitários e funcionais (integração).

### Testes Unitários

* Cobrem a lógica de negócio isolada, sem dependência do contexto web.
* Testam serviços como:

  * `UserService`
  * `FilmService`
  * `JwtService`
* Utilizam `JUnit` e `Mockito` para simular comportamentos e dependências.

### Testes Funcionais (Integração)

* Simulam requisições REST reais com autenticação.
* Validam o comportamento dos endpoints públicos e protegidos.
* Utilizam `Spring Boot Test` com `MockMvc` para garantir o fluxo completo de requisição-resposta.
* Cobrem cenários de sucesso, falha (400, 403, 404), paginação e validação de dados.

📁 Todos os testes estão localizados no diretório:

```
\src\test\java\br\ifsp\film_catalog
```

---

### ▶️ Como executar os testes

Você pode executar os testes de diferentes formas, conforme o ambiente de desenvolvimento utilizado:

#### No **VS Code**

1. Abra o projeto no VS Code com a extensão **Java Extension Pack** instalada.
2. Vá até a aba **"Testing"** (ícone de becher no lado esquerdo).
3. Clique em **"Run Test"** ao lado do nome da classe ou método.
4. Alternativamente, abra o arquivo de teste, passe o mouse sobre o método e clique no botão de execução ▶️.

#### No **IntelliJ IDEA**

1. Clique com o botão direito sobre a classe ou método de teste e selecione **Run 'NomeDoTeste'**.
2. Para rodar todos os testes:

   * Clique com o botão direito no diretório `src/test/java` e selecione **Run Tests in 'java'**.
   * Ou use o atalho: **Ctrl + Shift + F10** (Windows/Linux) ou **⌃⇧R** (macOS).

#### No **Terminal**

1. Certifique-se de que o projeto foi compilado corretamente.
2. Use o seguinte comando Maven:

```bash
./mvnw test
```

Ou, se estiver usando Maven globalmente instalado:

```bash
mvn test
```

Esse comando executa **todos os testes** (unitários e funcionais).


📬 Em caso de dúvidas ou sugestões, fique à vontade para abrir uma *issue* ou enviar um *pull request*!
