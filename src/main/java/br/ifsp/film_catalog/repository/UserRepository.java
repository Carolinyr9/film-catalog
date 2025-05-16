package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
