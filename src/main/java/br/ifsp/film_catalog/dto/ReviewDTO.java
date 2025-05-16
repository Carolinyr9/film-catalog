package br.ifsp.film_catalog.dto;

public class ReviewDTO {
    private Long id;
    private Long userId;
    private Long movieId;
    private Double nota;
    private Double notaDirecao;
    private Double notaFotografia;
    private String comentario;
    private int curtidas;
    private int denuncias;
    private boolean oculto;
    private Long watchlistId;

    public ReviewDTO() {}

    public ReviewDTO(Long id, Long userId, Long movieId, Double nota, Double notaDirecao, Double notaFotografia,
                     String comentario, int curtidas, int denuncias, boolean oculto) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.nota = nota;
        this.notaDirecao = notaDirecao;
        this.notaFotografia = notaFotografia;
        this.comentario = comentario;
        this.curtidas = curtidas;
        this.denuncias = denuncias;
        this.oculto = oculto;
    }

    public ReviewDTO(String comentario, Long watchlistId, Long userId, boolean oculto, Double notaDirecao,
                     Double nota, Long id, int denuncias, int curtidas, Long movieId, Double notaFotografia) {
        this.comentario = comentario;
        this.watchlistId = watchlistId;
        this.userId = userId;
        this.oculto = oculto;
        this.notaDirecao = notaDirecao;
        this.nota = nota;
        this.id = id;
        this.denuncias = denuncias;
        this.curtidas = curtidas;
        this.movieId = movieId;
        this.notaFotografia = notaFotografia;
    }

    public Long getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(Long watchlistId) {
        this.watchlistId = watchlistId;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isOculto() {
        return oculto;
    }

    public void setOculto(boolean oculto) {
        this.oculto = oculto;
    }

    public Double getNotaFotografia() {
        return notaFotografia;
    }

    public void setNotaFotografia(Double notaFotografia) {
        this.notaFotografia = notaFotografia;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public int getDenuncias() {
        return denuncias;
    }

    public void setDenuncias(int denuncias) {
        this.denuncias = denuncias;
    }

    public int getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(int curtidas) {
        this.curtidas = curtidas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Double getNotaDirecao() {
        return notaDirecao;
    }

    public void setNotaDirecao(Double notaDirecao) {
        this.notaDirecao = notaDirecao;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Getters e Setters
}
