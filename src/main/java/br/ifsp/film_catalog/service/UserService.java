package br.ifsp.film_catalog.service;

import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.UserPatchDTO;
import br.ifsp.film_catalog.dto.UserRequestDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.RoleRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public UserService(UserRepository userRepository,
                         RoleRepository roleRepository,
                         MovieRepository movieRepository,
                         PasswordEncoder passwordEncoder,
                         ModelMapper modelMapper,
                         PagedResponseMapper pagedResponseMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.movieRepository = movieRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(userPage, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new IllegalArgumentException("Username '" + userRequestDTO.getUsername() + "' already exists.");
        }
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + userRequestDTO.getEmail() + "' already exists.");
        }

        User user = modelMapper.map(userRequestDTO, User.class);
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (userRequestDTO.getRoleIds() != null && !userRequestDTO.getRoleIds().isEmpty()) {
            roles = userRequestDTO.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
        }

        for (Role role : roles) {
            user.addRole(role);
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check for username conflict if username is being changed
        userRepository.findByUsername(userRequestDTO.getUsername()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(id)) {
                throw new IllegalArgumentException("Username '" + userRequestDTO.getUsername() + "' already exists.");
            }
        });

        // Check for email conflict if email is being changed
        userRepository.findByEmailIgnoreCase(userRequestDTO.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(id)) {
                throw new IllegalArgumentException("Email '" + userRequestDTO.getEmail() + "' already exists.");
            }
        });

        // Map fields from DTO to entity, excluding password for explicit handling
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setUsername(userRequestDTO.getUsername());

        // Handle password update separately if provided
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        }

        Set<Role> roles = new HashSet<>();
        if (userRequestDTO.getRoleIds() != null && !userRequestDTO.getRoleIds().isEmpty()) {
            roles = userRequestDTO.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
        }

        for (Role role : roles) {
            user.addRole(role);
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO patchUser(Long id, UserPatchDTO userPatchDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Name
        userPatchDTO.getName().ifPresent(name -> {
            if (name.isBlank()) throw new IllegalArgumentException("Name cannot be blank if provided.");
            user.setName(name);
        });

        // Email
        userPatchDTO.getEmail().ifPresent(email -> {
            if (email.isBlank()) throw new IllegalArgumentException("Email cannot be blank if provided.");
            userRepository.findByEmailIgnoreCase(email).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new IllegalArgumentException("Email '" + email + "' already exists.");
                }
            });
            user.setEmail(email);
        });

        // Username
        userPatchDTO.getUsername().ifPresent(username -> {
            if (username.isBlank()) throw new IllegalArgumentException("Username cannot be blank if provided.");
            userRepository.findByUsername(username).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new IllegalArgumentException("Username '" + username + "' already exists.");
                }
            });
            user.setUsername(username);
        });

        // Password
        userPatchDTO.getPassword().ifPresent(password -> {
            if (password.isBlank()) throw new IllegalArgumentException("Password cannot be blank if provided.");
            // Add password complexity validation here if needed, or rely on UserRequestDTO's for full updates
            user.setPassword(passwordEncoder.encode(password));
        });

        // Roles
        userPatchDTO.getRoleIds().ifPresent(roleIds -> {
            Set<Role> newRoles = roleIds.stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            for (Role role : newRoles) {
                user.addRole(role);
            }
        });

        User patchedUser = userRepository.save(user);
        return modelMapper.map(patchedUser, UserResponseDTO.class);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        // Add any other business logic before deletion if necessary (e.g., check for dependencies)
        userRepository.deleteById(id);
    }

    @Transactional
    public void addFavoriteMovie(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        user.addFavorite(movie);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavoriteMovie(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        user.removeFavorite(movie);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getFavoriteMovies(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        List<Movie> favoriteMovies = userRepository.findById(userId).get().getFavoriteMovies().stream()
                .map(favorite -> favorite.getMovie())
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), favoriteMovies.size());
        
        Page<Movie> page = new PageImpl<>(favoriteMovies.subList(start, end), pageable, favoriteMovies.size());

        return pagedResponseMapper.toPagedResponse(page, MovieResponseDTO.class);
    }
}
