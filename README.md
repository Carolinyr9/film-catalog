# 🎬 Catálogo de Filmes - API RESTful

## 📌 Enunciado do Projeto

Desenvolva uma **API RESTful** para um sistema de **catálogo de filmes com avaliações feitas por usuários autenticados**.

Cada filme possui:

* 🎞️ Título
* 📝 Sinopse
* 📅 Ano de lançamento
* 🏷️ Gênero(s)
* ⏱️ Duração
* 🔞 Classificação indicativa

Os usuários podem:

* Adicionar filmes a uma **watchlist pessoal**
* Marcar filmes como **assistidos**
* Criar **avaliações com múltiplos critérios** (roteiro, direção, fotografia, etc.)

**Apenas filmes marcados como assistidos podem ser avaliados.**

A API também permite:

* Descobrir filmes com **filtros e recomendações**
* Curtir ou denunciar avaliações
* Ocultar conteúdos após número excessivo de denúncias
* Acesso por **níveis de permissão** (USER e ADMIN)
* Proteção com **JWT Authentication**

---

## 🧩 Histórias de Usuário

### 🔐 Autenticação e Autorização

* Como visitante, quero **me cadastrar** com nome, e-mail e senha.
* Como usuário autenticado, quero **fazer login e receber um token JWT**.
* Como administrador, quero **definir o papel dos usuários (USER ou ADMIN)**.

### 🎬 Filmes e Catálogo

* Como administrador, quero **cadastrar, editar, excluir e listar filmes**.
* Como usuário, quero **visualizar filmes com filtros** (por título, gênero ou ano).
* Como usuário, quero **adicionar filmes à minha watchlist**.
* Como usuário, quero **marcar filmes como assistidos**.
* Como sistema, quero **impedir avaliações de filmes não assistidos**.

### 📊 Avaliações

* Como usuário, quero **avaliar um filme assistido com múltiplos critérios** e uma nota geral.
* Como usuário, quero **editar ou excluir minhas avaliações**.
* Como usuário, quero **curtir ou denunciar avaliações**.
* Como sistema, quero **ocultar avaliações com excesso de denúncias**.
* Como administrador, quero **visualizar e moderar avaliações**.

### 📈 Recomendações e Destaques

* Como usuário, quero **receber recomendações com base nas minhas avaliações e gêneros favoritos**.
* Como administrador, quero **listar os filmes mais bem avaliados ou mais comentados**.
* Como administrador, quero **gerar estatísticas por usuário** (avaliações, notas médias, curtidas).

---

## ✅ Requisitos de Testes

* ✔️ **Testes unitários** de serviços (avaliações, recomendações, controle de acesso)
* ✔️ **Testes funcionais** com `MockMvc` ou `TestRestTemplate`
* ✔️ **Casos de teste cobrindo regras e restrições**:

  * Só avaliar se assistido
  * Ocultação por denúncias
  * Rankings por nota ou interação

---

## 🌟 Extras 

* ⭐ Sistema de **filmes favoritos** (além da watchlist)
* 📄 **Exportação de avaliações** em PDF ou JSON
* 🧮 **Média ponderada das avaliações por critério**
