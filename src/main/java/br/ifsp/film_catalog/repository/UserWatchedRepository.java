package br.ifsp.film_catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.film_catalog.model.UserWatched;
import br.ifsp.film_catalog.model.key.UserMovieId;

public interface UserWatchedRepository extends JpaRepository<UserWatched, UserMovieId> {
    
}
