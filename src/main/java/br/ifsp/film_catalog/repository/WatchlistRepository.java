package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    Page<Watchlist> findAllByUser_Id(Long id, Pageable pageable);
}
