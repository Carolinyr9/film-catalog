package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.UserPatchDTO;
import br.ifsp.film_catalog.dto.UserRequestDTO;
import br.ifsp.film_catalog.dto.UserRequestWithRolesDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ErrorResponse;
import br.ifsp.film_catalog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuários", description = "API para gerenciamento de usuários")
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna uma lista paginada de todos os usuários.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserResponseDTO>> getAllUsers(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        PagedResponse<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna um único usuário pelo seu ID exclusivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário recuperado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Buscar usuário por nome de usuário (username)", description = "Retorna um único usuário pelo seu nome de usuário (username) exclusivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário recuperado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search/by-username")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #username)")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
        @RequestParam String username
    ) {
        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Registrar um novo usuário (público)", description = "Permite que qualquer visitante se registre. O usuário receberá o papel 'ROLE_USER' por padrão.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação (ex: username/email já existe)",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO createdUser = userService.createUser(userRequestDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Criar um novo usuário (Admin)", description = "Permite que um administrador crie um novo usuário, podendo especificar papéis. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso pelo administrador"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação (ex: username/email já existe, ID de role inválido)",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUserByAdmin(@Valid @RequestBody UserRequestWithRolesDTO userRequestWithRolesDTO) {
        UserResponseDTO createdUser = userService.createUser(userRequestWithRolesDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar um usuário existente (atualização total)", description = "Atualiza todos os campos de um usuário existente pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestWithRolesDTO userRequestDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, userRequestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Atualizar parcialmente um usuário existente", description = "Atualiza parcialmente campos de um usuário existente pelo seu ID usando semântica PATCH.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário parcialmente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)")
    public ResponseEntity<UserResponseDTO> patchUser(@PathVariable Long id, @Valid @RequestBody UserPatchDTO userPatchDTO) {
        UserResponseDTO patchedUser = userService.patchUser(id, userPatchDTO);
        return ResponseEntity.ok(patchedUser);
    }

    @Operation(summary = "Excluir um usuário", description = "Exclui um usuário pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Typically only admins can delete users
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adicionar um filme aos favoritos do usuário", description = "Adiciona um filme à lista de favoritos de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme favoritado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário ou filme não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping("/{userId}/favorites/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<Void> addFavoriteMovie(@PathVariable Long userId, @PathVariable Long movieId) {
        userService.addFavoriteMovie(userId, movieId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remover um filme dos favoritos do usuário", description = "Remove um filme da lista de favoritos de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme removido dos favoritos com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário ou filme não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/{userId}/favorites/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<Void> removeFavoriteMovie(@PathVariable Long userId, @PathVariable Long movieId) {
        userService.removeFavoriteMovie(userId, movieId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar filmes favoritos do usuário", description = "Retorna uma lista paginada dos filmes favoritos de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes favoritos recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/{userId}/favorites")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<PagedResponse<MovieResponseDTO>> getFavoriteMovies(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        PagedResponse<MovieResponseDTO> favoriteMovies = userService.getFavoriteMovies(userId, pageable);
        return ResponseEntity.ok(favoriteMovies);
    }
}
