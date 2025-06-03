package br.ifsp.film_catalog.functionalTests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.ifsp.film_catalog.exception.GlobalExceptionHandler;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.MovieService;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Mock
    private MovieService movieService;

    private String token;

    private String id;

    private String title;

    private String genre;

    private int year;

    @BeforeEach
    void setUp() throws Exception  {
        movieRepository.deleteAll();

        Genre genre1 = new Genre("Drama");
        Genre genre2 = new Genre("Ação");
        Genre genre3 = new Genre("Comédia");
        Genre genre4 = new Genre("Animação");

        Movie movie1 = new Movie();
        movie1.setTitle("O Poderoso Chefão");   
        movie1.setSynopsis("História da família mafiosa Corleone");
        movie1.setReleaseYear(1972);
        movie1.setDuration(175);
        movie1.setContentRating(ContentRating.A16);
        movie1.getGenres().add(genre1);
        movie1.getGenres().add(genre2);
        movieRepository.save(movie1);
        Movie movie2 = new Movie();
        movie2.setTitle("Forrest Gump");
        movie2.setSynopsis("A vida de um homem com QI abaixo da média");
        movie2.setReleaseYear(1994);
        movie2.setDuration(142);
        movie2.setContentRating(ContentRating.A14);
        movie2.getGenres().add(genre1);
        movie2.getGenres().add(genre3);
        movieRepository.save(movie2);
        Movie movie3 = new Movie();
        movie3.setTitle("A Origem");
        movie3.setSynopsis("Um ladrão que rouba segredos ao invadir sonhos");
        movie3.setReleaseYear(2010);
        movie3.setDuration(148);
        movie3.getGenres().add(genre2);
        movie3.getGenres().add(genre1);
        movie3.setContentRating(ContentRating.A12);
        movieRepository.save(movie3);
        Movie movie4 = new Movie();
        movie4.setTitle("Toy Story");
        movie4.setSynopsis("Brinquedos ganham vida quando ninguém está olhando");
        movie4.setReleaseYear(1995);
        movie4.setDuration(81);
        movie4.setContentRating(ContentRating.A10);
        movie4.getGenres().add(genre3);
        movie4.getGenres().add(genre4);
        movieRepository.save(movie4);
        Movie movie5 = new Movie();
        movie5.setTitle("Clube da Luta");
        movie5.setSynopsis("Homem cria clube secreto de lutas");
        movie5.setReleaseYear(1999);
        movie5.setDuration(139);
        movie5.setContentRating(ContentRating.A18);
        movie5.getGenres().add(genre1);
        movie5.getGenres().add(genre2);
        movieRepository.save(movie5);

        Long movieId1 = movie1.getId();
        id = String.valueOf(movieId1);
        title = "clube";
        genre = genre1.getName();
        year = 1999;

        System.out.println("Filmes criados: " + movieRepository.count());
        System.out.println(movie1.getId());

        token = autenticarEObterToken();
    }

    private String autenticarEObterToken() throws Exception {
        String loginRequest = """
            {
                "username": "admin",
                "password": "password"
            }
        """;

        MvcResult result = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = responseBody;

        return token;
    }


    @Test
    @DisplayName("Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() throws Exception {
        String loginRequest = """
            {
                "username": "admin",
                "password": "password"
            }
        """;

        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    @DisplayName("Deve retornar todos os filmes")
    void shouldReturnAllMovies() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/movies")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("A Origem"))
                .andExpect(jsonPath("$.content[1].title").value("Clube da Luta"))
                .andExpect(jsonPath("$.content[2].title").value("Forrest Gump"))
                .andExpect(jsonPath("$.content[3].title").value("O Poderoso Chefão"))
                .andExpect(jsonPath("$.content[4].title").value("Toy Story"));
    }

    @Test
    @DisplayName("Deve retornar um filme por ID")
    void shouldReturnMovieById() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/movies/" + this.id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("O Poderoso Chefão"))
                .andExpect(jsonPath("$.synopsis").value("História da família mafiosa Corleone")) 
                .andExpect(jsonPath("$.releaseYear").value(1972))
                .andExpect(jsonPath("$.duration").value(175))
                .andExpect(jsonPath("$.contentRating").value("A16"));
    }

    // @Test
    // @DisplayName("Deve retornar filmes por título")
    // void shouldReturnMoviesByTitle() throws Exception {
    //     token = autenticarEObterToken();
    //     mockMvc.perform(get("/api/movies/search/by-title?title={title}", this.title)
    //             .header("Authorization", "Bearer " + token))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.content[0].title").value("Clube da Luta"));
    // }

    @Test
    @DisplayName("Deve retornar filmes por gênero")
    void shouldReturnMoviesByGenre() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/movies/search/by-genre?genreName="+ this.genre)
                .param("page", "0")
                .param("size", "10")
        .header("Authorization", "Bearer " + token))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.content[0].title").exists());

    }

    @Test
    @DisplayName("Deve retornar filmes por ano de lançamento")
    void shouldReturnMoviesByReleaseYear() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/movies/search/by-year?year="+ this.year)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Clube da Luta"));
    }

    @Test
    @DisplayName("Deve retornar um filme criado com sucesso")
    void shouldCreateMovieSuccessfully() throws Exception {
        token = autenticarEObterToken();
        String newMovieJson = """
            {
                "title": "Interestelar",
                "synopsis": "Exploração espacial em busca de um novo lar",
                "releaseYear": 2014,
                "duration": 169,
                "contentRating": "A12",
                "genres": ["Drama"]
            }
        """;
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newMovieJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Interestelar"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar filme com título em branco")
    void shouldReturnErrorWhenCreatingMovieWithBlankTitle() throws Exception {
        token = autenticarEObterToken();
        String newMovieJson = """
            {
                "title": "",
                "synopsis": "Exploração espacial em busca de um novo lar",
                "releaseYear": 2014,
                "duration": 169,
                "contentRating": "A12",
                "genres": ["Drama"]
            }
        """;
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newMovieJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar um filme atuliazado com sucesso")
    void shouldUpdateMovieSuccessfully() throws Exception {
        token = autenticarEObterToken();
        String updatedMovieJson = """
            {
                "title": "Interestelar",
                "synopsis": "Exploração espacial em busca de um novo lar",
                "releaseYear": 2014,
                "duration": 169,
                "contentRating": "A12",
                "genres": ["Drama"]
            }
        """;
        mockMvc.perform(put("/api/movies/" + this.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedMovieJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Interestelar"));
    }

    @Test
    @DisplayName("Deve deletar o filme por ID")
    void shouldDeleteMovieById() throws Exception {
        token = autenticarEObterToken();
        mockMvc.perform(get("/api/movies/"+this.id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());    
    }

}

