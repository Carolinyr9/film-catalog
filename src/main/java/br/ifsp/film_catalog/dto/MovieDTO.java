package br.ifsp.film_catalog.dto;

import java.util.List;

public class MovieDTO {
    private Long id;
    private String titulo;
    private List<String> generos;
    private int anoLancamento;
    private int duracao;
    private String classificacaoIndicativa;

    public MovieDTO() {}

    public MovieDTO(Long id, String titulo, List<String> generos, int anoLancamento, int duracao, String classificacaoIndicativa) {
        this.id = id;
        this.titulo = titulo;
        this.generos = generos;
        this.anoLancamento = anoLancamento;
        this.duracao = duracao;
        this.classificacaoIndicativa = classificacaoIndicativa;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(int anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getGeneros() {
        return generos;
    }

    public void setGeneros(List<String> generos) {
        this.generos = generos;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public String getClassificacaoIndicativa() {
        return classificacaoIndicativa;
    }

    public void setClassificacaoIndicativa(String classificacaoIndicativa) {
        this.classificacaoIndicativa = classificacaoIndicativa;
    }
}
