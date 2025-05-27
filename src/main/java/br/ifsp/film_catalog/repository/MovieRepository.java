package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    Page<Movie> findByGenerosContainingIgnoreCase(String genero, Pageable pageable);

    Page<Movie> findByAnoLancamentoContainingIgnoreCase(int ano, Pageable pageable);
}
