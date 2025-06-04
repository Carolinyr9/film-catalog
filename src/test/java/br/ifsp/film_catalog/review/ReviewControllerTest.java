package br.ifsp.film_catalog.review;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.film_catalog.controller.ReviewController;
import br.ifsp.film_catalog.service.ContentFlagService;
import br.ifsp.film_catalog.service.ReviewService;

import org.springdoc.core.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ContentFlagService contentFlagService;
    
    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    

}