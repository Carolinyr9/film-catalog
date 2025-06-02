package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreRequestDTO {
    @NotBlank(message = "Genre name cannot be blank.")
    @Size(max = 20, message = "Genre name cannot exceed 20 characters.")
    private String name;
}