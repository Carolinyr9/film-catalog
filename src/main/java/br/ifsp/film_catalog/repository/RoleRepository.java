package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName roleName);
}
