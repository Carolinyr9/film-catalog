package br.ifsp.film_catalog.config;

import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.repository.RoleRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("--- CHECKING/INITIALIZING ADMIN USER ---");

        if (!userRepository.existsByUsername("admin")) {
            log.info("Admin user not found. Creating...");

            Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("CRITICAL ERROR: ROLE_ADMIN not found. RoleInitializer may have failed."));

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setName("Administrator");
            adminUser.setEmail("admin@filmcatalog.com");
            
            adminUser.setPassword(passwordEncoder.encode("AdminPassword123!"));
            
            adminUser.addRole(adminRole);

            userRepository.save(adminUser);
            log.info("Admin user created successfully.");
        } else {
            log.info("Admin user already exists. Skipping creation.");
        }
        
        log.info("--- ADMIN USER INITIALIZATION COMPLETE ---");
    }
}