package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.UserDTO;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper userMapper;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService, ModelMapper userMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Operation(summary = "Criar novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou nome de usuário já existe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nome de usuário já existe");
        }

        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso");
    }

    @Operation(summary = "Listar todos os usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userRepository.findAll(pageable)
                .map(user -> userMapper.map(user, UserDTO.class));
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(userMapper.map(user, UserDTO.class));
    }

    @Operation(summary = "Excluir usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza um usuário específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        userMapper.map(dto, user);
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.map(user, UserDTO.class));
    }

    @Operation(summary = "Atualizar parcialmente usuário", description = "Atualiza parcialmente os dados de um usuário específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDTO> patchUser(@PathVariable Long userId, @RequestBody UserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        // atualize outros campos que podem ser alterados parcialmente
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.map(user, UserDTO.class));
    }

    @Operation(summary = "Buscar usuário por username", description = "Retorna um usuário específico pelo username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return ResponseEntity.ok(userMapper.map(user, UserDTO.class));
    }

}
