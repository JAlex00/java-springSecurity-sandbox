package com.alex.springSecurity.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static com.alex.springSecurity.security.ApplicationUserPermission.*;
import static com.alex.springSecurity.security.ApplicationUserRole.*;
import static org.springframework.security.config.Customizer.withDefaults;

// Classe dedita alla configurazione di tutto ciò che ha a che fare con la sicurezza
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Per abilitare il @PreAuthorize in StudentManagementController
public class ApplicationSecurityConfig {

    // Inject dell'encoder
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ApplicationSecurityConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /** Configurazione
     * Qualsiasi Client che chiami un'API (anyRequest),
     * dovra' autenticarsi (authenticated)
     * tramite Basic Authentication (httpBasic)
     * ---
     * N.B. user=user; psw=[viene loggata in console]
     * Una volta inseriti, user e password verranno passati ad ogni richiesta in formato base64
     * (vedi esempio con Postman)
     * ---
     * N.B. CSRF (Cross-Site Request Forgery)
     * Quando il CSRF è attivo, Spring Security blocca i metodi "Unsafe"
     * (POST, PUT, DELETE), che modificano lo stato
     * a meno che la richiesta non includa un CSRF TOKEN valido.
     * Metodi "Safe", come GET, possono essere richiamati
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable) // Disabilito il CSRF
                .authorizeHttpRequests(auth -> auth
                //.requestMatchers("/", "/css/*", "/js/*").permitAll() -- No Auth per risorse indicate
                .requestMatchers("/api/**").hasRole(STUDENT.name()) // Solo utenti con RUOLO "STUDENT" chiamano le API indicate
                .requestMatchers(HttpMethod.POST, "/management/api/**").hasAnyAuthority(STUDENT_WRITE.getPermission()) // Solo utenti con PERMISSION "STUDENT_WRITE" chiamano le API indicate
                .anyRequest().authenticated()
        ).httpBasic(withDefaults());
        return http.build();
    }

    /** Creo utenze in memory, con le quali potro' superare l'autenticazione (a seconda del ruolo)
     * N.B. la password deve essere codificata,
     * altrimenti verra' sollevata l'eccezione "There is no PasswordEncoder ..."
     */
    @Bean
    protected UserDetailsService userDetailsService() {

        // Questo utente ha un ruolo STUDENT, che NON ha permessi
        UserDetails gokuUser = User.builder()
                .username("goku")
                .password(passwordEncoder.encode("password"))
                //.roles(STUDENT.name()) -- imposto solo il RUOLO
                .authorities(STUDENT.getGrantedAuthorities()) // imposto RUOLO e PERMISSION
                .build();

        // Questo utente ha un ruolo ADMIN, che ha tutti i permessi
        UserDetails shenronUser = User.builder()
                .username("shenron")
                .password(passwordEncoder.encode("password123"))
                //.roles(ADMIN.name())
                .authorities(ADMIN.getGrantedAuthorities())
                .build();

        // Questo utente ha un ruolo ADMINTRAINEE, con solo permessi di scrittura
        UserDetails gohanUser = User.builder()
                .username("gohan")
                .password(passwordEncoder.encode("password234"))
                //.roles(ADMINTRAINEE.name())
                .authorities(ADMINTRAINEE.getGrantedAuthorities())
                .build();

        return new InMemoryUserDetailsManager(gokuUser, shenronUser, gohanUser);
    }
    // Ora accedo con es. user=goku; password=password
}
