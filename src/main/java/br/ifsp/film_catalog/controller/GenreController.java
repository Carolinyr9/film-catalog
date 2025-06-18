package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.GenreRequestDTO;
import br.ifsp.film_catalog.dto.GenreResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ErrorResponse;
import br.ifsp.film_catalog.service.GenreService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize; // For security

@Tag(name = "Gêneros", description = "API para gerenciamento de gêneros de filmes")
@Validated
@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @Operation(summary = "Listar todos os gêneros", description = "Retorna uma lista paginada de todos os gêneros de filmes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de gêneros recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponse<GenreResponseDTO>> getAllGenres(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(genreService.getAllGenres(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar gênero por ID", description = "Retorna um único gênero pelo seu ID exclusivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gênero recuperado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado", 
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GenreResponseDTO> getGenreById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @Operation(summary = "Criar um novo gênero", description = "Cria um novo gênero de filme. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Gênero criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação (ex: nome do gênero já existe ou está em branco)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponseDTO> createGenre(@Valid @RequestBody GenreRequestDTO genreRequestDTO) {
        GenreResponseDTO createdGenre = genreService.createGenre(genreRequestDTO);
        return new ResponseEntity<>(createdGenre, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar um gênero existente", description = "Atualiza um gênero de filme existente pelo seu ID. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gênero atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação (ex: novo nome do gênero conflita com existente)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponseDTO> updateGenre(@PathVariable Long id, @Valid @RequestBody GenreRequestDTO genreRequestDTO) {
        GenreResponseDTO updatedGenre = genreService.updateGenre(id, genreRequestDTO);
        return ResponseEntity.ok(updatedGenre);
    }

    @Operation(summary = "Excluir um gênero", description = "Exclui um gênero de filme pelo seu ID. Falha se o gênero estiver associado a algum filme. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Gênero excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito - Gênero está em uso e não pode ser excluído", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        try {
            genreService.deleteGenre(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}