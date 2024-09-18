package com.ucv.ace.configurations;

import com.ucv.ace.security.JWTFilter;
import com.ucv.ace.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final UserService userService;

    private final JWTFilter jwtTokenFilter;

    private final AuthenticationConfiguration configuration;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.rememberMe(withDefaults());
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder
                .userDetailsService(userService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }
}
