package br.ifsp.film_catalog.config;

import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.enums.RoleName; //
import br.ifsp.film_catalog.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- CHECKING/INITIALIZING ROLES ---");
        for (RoleName roleNameEnum : RoleName.values()) {
            createRoleIfNotFound(roleNameEnum);
        }
        System.out.println("--- ROLES INITIALIZATION COMPLETE ---");
    }

    private Role createRoleIfNotFound(RoleName roleNameEnum) {
        return roleRepository.findByRoleName(roleNameEnum) // Assumes findByRoleName exists in RoleRepository
                .orElseGet(() -> {
                    Role newRole = new Role(roleNameEnum); // Uses constructor from Role.java
                    System.out.println("Creating role: " + roleNameEnum);
                    return roleRepository.save(newRole);
                });
    }
}