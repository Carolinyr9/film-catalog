package br.ifsp.film_catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.film_catalog.model.ContentFlag;
import br.ifsp.film_catalog.model.key.UserReviewId;

public interface ContentFlagRepository extends JpaRepository<ContentFlag, UserReviewId> {
    boolean existsById(UserReviewId id);

}
