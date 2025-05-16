package br.ifsp.film_catalog.dto;

import br.ifsp.film_catalog.model.RoleName;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class UserDTO {
    private Long id;
    private String nome;

    @NotBlank(message = "O e-mail não pode estar vazio")
    private String email;

    @NotBlank(message = "O nome de usuário não pode estar vazio")
    private String username;

    private Set<RoleName> roles;

    @NotBlank(message = "A senha não pode estar vazia")
    private String password;

    public UserDTO() {}

    public UserDTO(Long id, String nome, String email, String username, Set<RoleName> roles, String password) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.password = password;
    }

    // Getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleName> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
