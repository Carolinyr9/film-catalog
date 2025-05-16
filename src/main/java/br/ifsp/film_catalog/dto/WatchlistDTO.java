package br.ifsp.film_catalog.dto;

import java.util.List;

public class WatchlistDTO {
    private Long id;
    private String nome;
    private boolean assistido;
    private Long userId;
    private List<Long> filmesIds;

    public WatchlistDTO() {}

    public WatchlistDTO(Long id, String nome, boolean assistido, Long userId, List<Long> filmesIds) {
        this.id = id;
        this.nome = nome;
        this.assistido = assistido;
        this.userId = userId;
        this.filmesIds = filmesIds;
    }

    public boolean isAssistido() {
        return assistido;
    }

    public void setAssistido(boolean assistido) {
        this.assistido = assistido;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getFilmesIds() {
        return filmesIds;
    }

    public void setFilmesIds(List<Long> filmesIds) {
        this.filmesIds = filmesIds;
    }
}
