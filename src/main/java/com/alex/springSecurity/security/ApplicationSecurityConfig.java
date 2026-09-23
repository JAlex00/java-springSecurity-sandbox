package com.alex.springSecurity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

// Classe dedita alla configurazione di tutto ciò che ha a che fare con la sicurezza
@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig {

    // Configurazione per consentire la basic authentication
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
        ).httpBasic(withDefaults());
        return http.build();
    }
}
