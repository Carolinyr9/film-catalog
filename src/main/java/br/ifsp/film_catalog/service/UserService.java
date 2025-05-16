package br.ifsp.film_catalog.service;

import br.ifsp.film_catalog.dto.UserDTO;
import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.RoleRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username já existe");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Set<Role> roles = dto.getRoles().stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return userRepository.save(user);
    }
}


