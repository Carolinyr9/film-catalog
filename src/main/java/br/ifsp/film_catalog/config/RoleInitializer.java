package br.ifsp.film_catalog.config;

import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class RoleInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("--- CHECKING/INITIALIZING ROLES ---");
        for (RoleName roleNameEnum : RoleName.values()) {
            createRoleIfNotFound(roleNameEnum);
        }
        log.info("--- ROLES INITIALIZATION COMPLETE ---");
    }

    private Role createRoleIfNotFound(RoleName roleNameEnum) {
        return roleRepository.findByRoleName(roleNameEnum) // Assumes findByRoleName exists in RoleRepository
                .orElseGet(() -> {
                    Role newRole = new Role(roleNameEnum); // Uses constructor from Role.java
                    log.info("Creating role: " + roleNameEnum);
                    return roleRepository.save(newRole);
                });
    }
}