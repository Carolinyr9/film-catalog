package br.ifsp.film_catalog.dto;

import br.ifsp.film_catalog.model.User;

public class UserPatchDTO {

    private String nome;
    private String email;
    private User.Tipo tipo;

    // Getters e setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User.Tipo getTipo() {
        return tipo;
    }

    public void setTipo(User.Tipo tipo) {
        this.tipo = tipo;
    }
}

