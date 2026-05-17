package cl.duoc.StoreGo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import cl.duoc.StoreGo.service.AuthService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public final AuthService authService;

    public SecurityConfig(AuthService authService) {
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/guns/lista").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new TokenValidationFilter(authService),
            UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
