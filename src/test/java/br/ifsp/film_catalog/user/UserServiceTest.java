package br.ifsp.film_catalog.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.ifsp.film_catalog.model.*;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.model.key.UserMovieId;
import br.ifsp.film_catalog.repository.*;
import br.ifsp.film_catalog.service.UserService;
import br.ifsp.film_catalog.dto.*;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import br.ifsp.film_catalog.exception.ResourceNotFoundException;

@ActiveProfiles("test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserFavoriteRepository userFavoriteRepository;

    @Mock
    private UserWatchedRepository userWatchedRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById_found() {
        User user = new User();
        user.setId(1L);
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResponseDTO.class)).thenReturn(dto);

        UserResponseDTO response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
        assertTrue(ex.getMessage().contains("User not found with id"));
    }

    @Test
    void testCreateUser_success() {
        UserRequestWithRolesDTO userRequestWithRolesDTO = new UserRequestWithRolesDTO();
        userRequestWithRolesDTO.setUsername("john");
        userRequestWithRolesDTO.setEmail("john@example.com");
        userRequestWithRolesDTO.setPassword("pass123");
        userRequestWithRolesDTO.setRoles(Set.of(new RoleRequestDTO("ROLE_USER")));

        User userEntity = new User();
        userEntity.setUsername("john");

        Role role = new Role();
        role.setRoleName(RoleName.ROLE_USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("john");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUsername("john");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(modelMapper.map(userRequestWithRolesDTO, User.class)).thenReturn(userEntity);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResponseDTO.class)).thenReturn(responseDTO);

        UserResponseDTO created = userService.createUser(userRequestWithRolesDTO);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("john", created.getUsername());

        verify(userRepository).existsByUsername("john");
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(userEntity);
    }

    @Test
    void testCreateUser_usernameExists() {
        UserRequestWithRolesDTO dto = new UserRequestWithRolesDTO();
        dto.setUsername("john");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
        assertTrue(ex.getMessage().contains("Username 'john' already exists."));
    }

    @Test
    void testDeleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        assertTrue(ex.getMessage().contains("User not found with id"));
    }

    @Test
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(1L);
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, UserResponseDTO.class))
                .thenReturn(new PagedResponse<UserResponseDTO>(
                        Collections.emptyList(),
                        page.getNumber(),     
                        page.getSize(),      
                        page.getTotalElements(),
                        page.getTotalPages(),   
                        page.isLast()           
                ));

        PagedResponse<UserResponseDTO> response = userService.getAllUsers(pageable);

        assertNotNull(response);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testAddFavoriteMovie_success() {
        Long userId = 1L;
        Long movieId = 10L;

        User user = new User();
        user.setId(userId);
        Movie movie = new Movie();
        movie.setId(movieId);

        when(userFavoriteRepository.existsById(new UserMovieId(userId, movieId))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(userFavoriteRepository.save(any(UserFavorite.class))).thenReturn(null);

        assertDoesNotThrow(() -> userService.addFavoriteMovie(userId, movieId));

        verify(userFavoriteRepository).save(any(UserFavorite.class));
    }

    @Test
    void testAddFavoriteMovie_alreadyFavorite() {
        Long userId = 1L;
        Long movieId = 10L;

        when(userFavoriteRepository.existsById(new UserMovieId(userId, movieId))).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.addFavoriteMovie(userId, movieId));
        assertTrue(ex.getMessage().contains("already a favorite"));
    }
}
