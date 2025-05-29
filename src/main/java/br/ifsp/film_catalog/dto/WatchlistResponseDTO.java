package br.ifsp.film_catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private Set<MovieResponseDTO> movies = new HashSet<>();
}
