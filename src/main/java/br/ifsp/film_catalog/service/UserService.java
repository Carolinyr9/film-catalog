package br.ifsp.film_catalog.service;

import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.RoleRequestDTO;
import br.ifsp.film_catalog.dto.UserPatchDTO;
import br.ifsp.film_catalog.dto.UserRequestDTO;
import br.ifsp.film_catalog.dto.UserRequestWithRolesDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.UserFavorite;
import br.ifsp.film_catalog.model.UserWatched;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.model.key.UserMovieId;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.ReviewRepository;
import br.ifsp.film_catalog.repository.RoleRepository;
import br.ifsp.film_catalog.repository.UserFavoriteRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.UserWatchedRepository;

import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final UserWatchedRepository userWatchedRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public UserService(UserRepository userRepository,
                         RoleRepository roleRepository,
                         MovieRepository movieRepository,
                         UserFavoriteRepository userFavoriteRepository,
                         UserWatchedRepository userWatchedRepository,
                         PasswordEncoder passwordEncoder,
                         ModelMapper modelMapper,
                         PagedResponseMapper pagedResponseMapper, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.movieRepository = movieRepository;
        this.userFavoriteRepository = userFavoriteRepository;
        this.userWatchedRepository = userWatchedRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.reviewRepository = reviewRepository;
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
        Set<RoleRequestDTO> roles = new HashSet<>();
        roles.add(new RoleRequestDTO("ROLE_USER"));
        
        UserRequestWithRolesDTO userRequestWithRolesDTO = modelMapper.map(userRequestDTO, UserRequestWithRolesDTO.class);
        userRequestWithRolesDTO.setRoles(roles);

        return createUser(userRequestWithRolesDTO);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestWithRolesDTO userRequestDTO) {
        if (userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new IllegalArgumentException("Username '" + userRequestDTO.getUsername() + "' already exists.");
        }
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + userRequestDTO.getEmail() + "' already exists.");
        }

        User user = modelMapper.map(userRequestDTO, User.class);
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        for (RoleRequestDTO roleDTO : userRequestDTO.getRoles()) {
            Role role = roleRepository.findByRoleName(RoleName.fromString(roleDTO.getRoleName()))
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleDTO.getRoleName()));
            user.addRole(role);
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestWithRolesDTO userRequestDTO) {
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

        for (RoleRequestDTO roleDTO : userRequestDTO.getRoles()) {
            Role role = roleRepository.findByRoleName(RoleName.fromString(roleDTO.getRoleName()))
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleDTO.getRoleName()));
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
        if (userPatchDTO.getName() != null) {
            String name = userPatchDTO.getName();
            if (name.isBlank()) throw new IllegalArgumentException("Name cannot be blank if provided.");
            user.setName(name);
        }

        // Email
        if (userPatchDTO.getEmail() != null) {
            String email = userPatchDTO.getEmail();
            if (email.isBlank()) throw new IllegalArgumentException("Email cannot be blank if provided.");

            userRepository.findByEmailIgnoreCase(email).ifPresent(existingUserByEmail -> {
                if (!existingUserByEmail.getId().equals(id)) {
                    throw new IllegalArgumentException("Email '" + email + "' already exists.");
                }
            });
            user.setEmail(email);
        }

        // Username
        if (userPatchDTO.getUsername() != null) {
            String username = userPatchDTO.getUsername();
            if (username.isBlank()) throw new IllegalArgumentException("Username cannot be blank if provided.");

            userRepository.findByUsername(username).ifPresent(existingUserByUsername -> {
                if (!existingUserByUsername.getId().equals(id)) {
                    throw new IllegalArgumentException("Username '" + username + "' already exists.");
                }
            });
            user.setUsername(username);
        }

        // Password
        if (userPatchDTO.getPassword() != null) {
            String password = userPatchDTO.getPassword();
            if (password.isBlank()) throw new IllegalArgumentException("Password cannot be blank if provided.");
            user.setPassword(passwordEncoder.encode(password));
        }

        // Roles
        if (userPatchDTO.getRoleIds() != null) {
            Set<Role> newRoles = userPatchDTO.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            user.getRoles().clear();  // Limpa roles atuais antes
            for (Role role : newRoles) {
                user.addRole(role);
            }
        }

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
        UserMovieId favoriteId = new UserMovieId(userId, movieId);
        if (userFavoriteRepository.existsById(favoriteId)) {
            throw new IllegalArgumentException("Movie with id " + movieId + " is already a favorite for user with id " + userId);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        UserFavorite favorite = new UserFavorite(user, movie);
        userFavoriteRepository.save(favorite);
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

    @Transactional
    public void addWatchedMovie(Long userId, Long movieId) {
        UserMovieId watchedId = new UserMovieId(userId, movieId);
        if (userWatchedRepository.existsById(watchedId)) {
            throw new IllegalArgumentException("Movie with id " + movieId + " is already marked as watched for user with id " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
        
        UserWatched watched = new UserWatched(user, movie, LocalDateTime.now());
        userWatchedRepository.save(watched);
    }

    @Transactional
    public void removeWatchedMovie(Long userId, Long movieId) {
        UserMovieId watchedId = new UserMovieId(userId, movieId);
        if (userWatchedRepository.existsById(watchedId)) {
            userWatchedRepository.deleteById(watchedId);
            
            reviewRepository.deleteByUserWatchedMovieId(movieId);
        } else {
            throw new ResourceNotFoundException("Watched record not found for user " + userId + " and movie " + movieId);
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<MovieResponseDTO> getWatchedMovies(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = userRepository.findById(userId).get();
        
        List<Movie> watchedMoviesList = user.getWatchedMovies().stream()
                .map(UserWatched::getMovie)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), watchedMoviesList.size());
        
        Page<Movie> pageResult = new PageImpl<>(watchedMoviesList.subList(start, end), pageable, watchedMoviesList.size());

        return pagedResponseMapper.toPagedResponse(pageResult, MovieResponseDTO.class);
    }

    public List<Genre> getTopGenresForUser(Long userId, int limit) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = userRepository.findById(userId).get();

        List<Movie> watched = user.getWatchedMovies().stream()
            .map(UserWatched::getMovie).toList();
        
        List<Movie> favorites = user.getFavoriteMovies().stream()
            .map(UserFavorite::getMovie).toList();

        List<Movie> all = Stream.concat(watched.stream(), favorites.stream()).toList();

        Map<Genre, Long> genreFrequency = all.stream()
            .flatMap(movie -> movie.getGenres().stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return genreFrequency.entrySet().stream()
            .sorted(Map.Entry.<Genre, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();
    }

    public PagedResponse<MovieResponseDTO> getRecommendedMoviesByGenres(List<Genre> genres, Pageable pageable){
        if (genres == null || genres.isEmpty()) {
            throw new IllegalArgumentException("Genres list cannot be null or empty.");
        }

        List<Movie> recommendedMovies = movieRepository.findByGenresContaining(genres.get(0), pageable)
                .stream()
                .filter(movie -> movie.getGenres().stream().anyMatch(genres::contains))
                .collect(Collectors.toList());

        Page<Movie> moviePage = new PageImpl<>(recommendedMovies, pageable, recommendedMovies.size());
        return pagedResponseMapper.toPagedResponse(moviePage, MovieResponseDTO.class);
    }

}
