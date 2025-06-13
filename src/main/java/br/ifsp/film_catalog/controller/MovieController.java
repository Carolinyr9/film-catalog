package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ErrorResponse;
import br.ifsp.film_catalog.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "Filmes", description = "API para gerenciamento de filmes no catálogo")
@Validated
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @Operation(summary = "Listar todos os filmes", description = "Retorna uma lista paginada de todos os filmes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponse<MovieResponseDTO>> getAllMovies(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        PagedResponse<MovieResponseDTO> movies = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Buscar filme por ID", description = "Retorna um único filme pelo seu ID exclusivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme recuperado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> getMovieById(@PathVariable @Valid Long id) {
        MovieResponseDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @Operation(summary = "Buscar filmes por título", description = "Retorna uma lista paginada de filmes cujos títulos contêm o termo de busca (ignora maiúsculas/minúsculas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search/by-title")
    public ResponseEntity<PagedResponse<MovieResponseDTO>> getMoviesByTitle(
        @RequestParam String title,
        @PageableDefault(size = 10, sort = "title") Pageable pageable
    ) {
        PagedResponse<MovieResponseDTO> movies = movieService.getMoviesByTitle(title, pageable);
        return ResponseEntity.ok(movies);
    }


    @Operation(summary = "Buscar filmes por nome do gênero", description = "Retorna uma lista paginada de filmes pertencentes a um gênero cujo nome contém o termo de busca (ignora maiúsculas/minúsculas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado com base no nome fornecido", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search/by-genre")
    public ResponseEntity<PagedResponse<MovieResponseDTO>> getMoviesByGenre(
        @RequestParam @NotBlank(message = "Genre name must not be blank") String genreName,
        @PageableDefault(size = 10, sort = "title") Pageable pageable
    ) {
        PagedResponse<MovieResponseDTO> movies = movieService.getMoviesByGenre(genreName, pageable);
        return ResponseEntity.ok(movies);
    }
    
    @Operation(summary = "Buscar filmes por ano de lançamento", description = "Retorna uma lista paginada de filmes lançados no ano especificado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search/by-year")
    public ResponseEntity<PagedResponse<MovieResponseDTO>> getMoviesByReleaseYear(
        @RequestParam int year,
        @PageableDefault(size = 10, sort = "title") Pageable pageable
    ) {
        PagedResponse<MovieResponseDTO> movies = movieService.getMoviesByReleaseYear(year, pageable);
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Criar um novo filme", description = "Cria um novo filme. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação (ex: título já existe, ID de gênero inválido)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))), // Pode ser Map<String, String> para erros de validação de campo
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MovieResponseDTO> createMovie(@Valid @RequestBody MovieRequestDTO movieRequestDTO) {
        MovieResponseDTO createdMovie = movieService.createMovie(movieRequestDTO);
        return new ResponseEntity<>(createdMovie, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar um filme existente (atualização total)", description = "Atualiza todos os campos de um filme existente pelo seu ID. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))), // Ou Map<String, String>
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieRequestDTO movieRequestDTO) {
        MovieResponseDTO updatedMovie = movieService.updateMovie(id, movieRequestDTO);
        return ResponseEntity.ok(updatedMovie);
    }

    @Operation(summary = "Atualizar parcialmente um filme existente", description = "Atualiza parcialmente campos de um filme existente pelo seu ID usando semântica PATCH. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme parcialmente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos / Erro de validação", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))), // Ou Map<String, String>
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    // @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> patchMovie(@PathVariable Long id, @Valid @RequestBody MoviePatchDTO moviePatchDTO) {
        MovieResponseDTO patchedMovie = movieService.patchMovie(id, moviePatchDTO);
        return ResponseEntity.ok(patchedMovie);
    }

     @Operation(summary = "Excluir um filme", description = "Exclui um filme pelo seu ID. Requer perfil de ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Filme excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer perfil de ADMIN)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    // @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar filmes em destaque", description = "Retorna uma lista paginada de filmes em destaque.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes recuperada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (se a segurança estiver habilitada)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/highlighted")
    public PagedResponse<MovieResponseDTO> getHighlightedMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return movieService.getHighlightedMovies(page, size);
    }

}
