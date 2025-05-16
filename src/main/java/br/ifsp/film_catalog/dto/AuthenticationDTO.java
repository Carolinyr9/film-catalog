package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationDTO {
    @NotBlank(message = "Please, enter your username.")
    private String username;
    @NotBlank(message = "Please, enter your password.")
    private String password;
}