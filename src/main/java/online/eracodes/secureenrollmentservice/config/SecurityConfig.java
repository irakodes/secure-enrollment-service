package online.eracodes.secureenrollmentservice.config;

import lombok.RequiredArgsConstructor;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final RegistrationAuthnProvider provider;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        // TODO: Change this before deployment
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/*/**").permitAll()
                        .requestMatchers("/register", "/public-key").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
