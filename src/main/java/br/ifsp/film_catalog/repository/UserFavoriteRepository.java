package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.UserFavorite;
import br.ifsp.film_catalog.model.key.UserMovieId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserMovieId> {
    
}
