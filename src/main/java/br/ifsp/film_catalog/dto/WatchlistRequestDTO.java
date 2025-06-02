package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistRequestDTO {

    @NotBlank(message = "Watchlist name cannot be blank.")
    @Size(max = 255, message = "Watchlist name cannot exceed 255 characters.")
    private String name;

    @Size(max = 1000, message = "Watchlist description cannot exceed 1000 characters.")
    private String description;
}
