package com.alex.springSecurity.security;

import com.alex.springSecurity.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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

    // Nuova versione - da passare a JwtUsernameAndPasswordAuthenticationFilter()
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** Configurazione Authentication
     * Qualsiasi Client che chiami un'API (anyRequest),
     * dovra' autenticarsi (authenticated)
     * tramite Basic Authentication (httpBasic)
     * OPPURE JWT Token
     * ---
     * N.B. CSRF (Cross-Site Request Forgery)
     * Quando il CSRF è attivo, Spring Security non autorizza le Request del client
     * a metodi "Unsafe"(POST, PUT, DELETE), che modificano lo stato,
     * a meno che la Request non includa un CSRF TOKEN valido (header X-XSRF-TOKEN).
     * Metodi "Safe", come GET, possono essere richiamati
     * (Questo viene fatto per evitare attacchi web tramite Link).
     *
     * N.B. Il CSRF va utilizzato solo per le Request provenienti dal Frontend
     * Per Request dirette alle API, conviene disabilitare il CSRF
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {

        // Solo Riga 58: Generazione del CSRF. withHttpOnlyFalse() indica che il token sara' invisibile nei cookie
        http
                //.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(AbstractHttpConfigurer::disable) // Disabilito il CSRF
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT Stateless
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authManager)) // Utilizzo del JWT Token
                .authorizeHttpRequests(auth -> auth
                        //.requestMatchers("/", "/css/*", "/js/*").permitAll() -- No Auth per risorse indicate
                        .requestMatchers("/api/**").hasRole(STUDENT.name()) // Solo utenti con RUOLO "STUDENT" chiamano le API indicate
                        .requestMatchers(HttpMethod.POST, "/management/api/**").hasAnyAuthority(STUDENT_WRITE.getPermission()) // Solo utenti con PERMISSION "STUDENT_WRITE" chiamano le API indicate
                        .anyRequest().authenticated()
                );
                //.httpBasic(withDefaults()); -- Non serce se si usa il JWT Token

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
