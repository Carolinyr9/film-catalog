package br.ifsp.film_catalog.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogMapper {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
