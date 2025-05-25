package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
}
