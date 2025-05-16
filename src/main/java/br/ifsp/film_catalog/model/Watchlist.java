package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Watchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome = "Watchlist";

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WatchlistMovie> filmes = new ArrayList<>();

    public Watchlist() {
    }

    public Watchlist(Long id, List<WatchlistMovie> filmes, String nome, User user) {
        this.id = id;
        this.filmes = filmes;
        this.nome = nome;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public List<WatchlistMovie> getFilmes() {
        return filmes;
    }

    public void setFilmes(List<WatchlistMovie> filmes) {
        this.filmes = filmes;
    }

    public void addMovie(Movie movie) {
        WatchlistMovie wm = new WatchlistMovie(this, movie, false);
        this.filmes.add(wm);
    }

    public void removeMovie(Movie movie) {
        this.filmes.removeIf(wm -> wm.getMovie().equals(movie));
    }

    public void removeAllMovies() {
        this.filmes.clear();
    }

    public boolean containsMovie(Optional<Movie> movie) {
        return this.filmes.stream().anyMatch(wm -> wm.getMovie().equals(movie));
    }

    public boolean movieIsWatched(Long id) {
        return this.filmes.stream()
                .anyMatch(wm -> wm.getMovie().getId().equals(id) && wm.isAssistido());
    }

    public void markMovieAsWatched(Movie movie) {
        this.filmes.stream().filter(wm -> wm.getMovie().equals(movie)).forEach(wm -> wm.setAssistido(true));
    }
}
