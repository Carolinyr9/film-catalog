package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "filme_id", nullable = false)
    private Movie movie;

    @NotNull
    private Double nota;

    private Double notaDirecao;

    private Double notaFotografia;

    @NotBlank
    private String comentario;

    private int curtidas;

    private int denuncias;

    private boolean oculta = false;

    private final int MIN_DENUNCIAS = 10;

    public Review() {
    }

    public Review(User user, boolean oculto, Double notaFotografia, Double nota, int denuncias, int curtidas, String comentario, Long id, Movie movie, Double notaDirecao) {
        this.user = user;
        this.oculta = oculto;
        this.notaFotografia = notaFotografia;
        this.nota = nota;
        this.denuncias = denuncias;
        this.curtidas = curtidas;
        this.comentario = comentario;
        this.id = id;
        this.movie = movie;
        this.notaDirecao = notaDirecao;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isOculta() {
        return oculta;
    }

    public void setOculta(boolean oculta) {
        this.oculta = oculta;
    }

    public Double getNotaDirecao() {
        return notaDirecao;
    }

    public void setNotaDirecao(Double notaDirecao) {
        this.notaDirecao = notaDirecao;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(int curtidas) {
        this.curtidas = curtidas;
    }

    public int getDenuncias() {
        return denuncias;
    }

    public void setDenuncias(int denuncias) {
        this.denuncias = denuncias;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Double getNotaFotografia() {
        return notaFotografia;
    }

    public void setNotaFotografia(Double notaFotografia) {
        this.notaFotografia = notaFotografia;
    }

    public int getMIN_DENUNCIAS() {
        return MIN_DENUNCIAS;
    }
}
