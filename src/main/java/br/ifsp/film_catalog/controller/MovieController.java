package br.ifsp.film_catalog.controller;

import br.ifsp.film_catalog.dto.MovieDTO;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.security.UserAuthenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Movies", description = "API para catálogo de filmes")
@Validated
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieRepository movieRepository;
    private final ModelMapper movieMapper;
    private final UserRepository userRepository;

    @Autowired
    public MovieController(MovieRepository movieRepository, ModelMapper movieMapper, UserRepository userRepository) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Listar filmes por título")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filmes encontrados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/titulo/{movieTitulo}")
    public ResponseEntity<Page<MovieDTO>> getMovieByTitulo(@PathVariable String movieTitulo, Pageable pageable) {
        var movies = movieRepository.findByTituloContainingIgnoreCase(movieTitulo, pageable)
                .map(movie -> movieMapper.map(movie, MovieDTO.class));
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Listar filmes por gênero")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filmes encontrados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/genero/{movieGenero}")
    public ResponseEntity<Page<MovieDTO>> getMovieByGenero(@PathVariable String movieGenero, Pageable pageable) {
        var movies = movieRepository.findByGeneroContainingIgnoreCase(movieGenero, pageable)
                .map(movie -> movieMapper.map(movie, MovieDTO.class));
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Listar filmes por ano de lançamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filmes encontrados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/ano/{movieAno}")
    public ResponseEntity<Page<MovieDTO>> getMovieByAnoLancamento(@PathVariable int movieAno, Pageable pageable) {
        var movies = movieRepository.findByAnoLancamentoContainingIgnoreCase(movieAno, pageable)
                .map(movie -> movieMapper.map(movie, MovieDTO.class));
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Listar todos os filmes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filmes encontrados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/all")
    public ResponseEntity<Page<MovieDTO>> getAllMovies(Pageable pageable) {
        var movies = movieRepository.findAll(pageable)
                .map(movie -> movieMapper.map(movie, MovieDTO.class));
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Buscar filme por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @GetMapping("/{movieID}")
    public ResponseEntity<MovieDTO> getMovieByID(@PathVariable Long movieID) {
        Movie movie = movieRepository.findById(movieID)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
        return ResponseEntity.ok(movieMapper.map(movie, MovieDTO.class));
    }

    @Operation(summary = "Criar novo filme")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<String> createMovie(@Validated @RequestBody MovieDTO dto,
                                              @AuthenticationPrincipal UserAuthenticated authentication) {
        User user = userRepository.findByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Movie movie = movieMapper.map(dto, Movie.class);
        movieRepository.save(movie);
        return ResponseEntity.ok("Filme criado com sucesso");
    }

    @Operation(summary = "Atualizar um filme (total)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{movieID}")
    public ResponseEntity<Void> updateMovie(@PathVariable Long movieID,
                                            @Validated @RequestBody MovieDTO dto) {
        if (!movieRepository.existsById(movieID)) {
            throw new ResourceNotFoundException("Filme não encontrado");
        }

        Movie movie = movieMapper.map(dto, Movie.class);
        movie.setId(movieID); // mantém o ID original
        movieRepository.save(movie);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar um filme (parcial)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{movieID}")
    public ResponseEntity<Void> updateMoviePartially(@PathVariable Long movieID,
                                                     @Validated @RequestBody MovieDTO dto) {
        Movie movie = movieRepository.findById(movieID)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));

        movieMapper.map(dto, movie); // atualiza apenas os campos do DTO não nulos
        movieRepository.save(movie);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Excluir um filme")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Filme excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{movieID}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieID) {
        if (!movieRepository.existsById(movieID)) {
            throw new ResourceNotFoundException("Filme não encontrado");
        }
        movieRepository.deleteById(movieID);
        return ResponseEntity.noContent().build();
    }
}
