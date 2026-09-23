package com.campusconnect.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/departments/**").permitAll()
                        .requestMatchers("/api/years/**").permitAll()
                        .requestMatchers("/api/semesters/**").permitAll()
                        .requestMatchers("/api/sections/**").permitAll()
                        .requestMatchers("/api/subjects/**").permitAll()
                        .requestMatchers("/api/teachers/**").permitAll()

                        // Bus Pass access
                        .requestMatchers("/api/admin/bus-passes/**").hasRole("ADMIN")
                        .requestMatchers("/api/bus-passes/**").hasRole("STUDENT")

                        // Feedback access
                        .requestMatchers("/api/admin/feedback/**").hasRole("ADMIN")
                        .requestMatchers("/api/feedback/**").hasRole("STUDENT")

                        // Notification access
                        .requestMatchers("/api/notifications/**").hasAnyRole("STUDENT", "TEACHER")

                        // Attendance access
                        .requestMatchers("/api/attendance/student/**")
                                .hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers("/api/attendance/class/**")
                                .hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/api/attendance/**")
                                .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        // Assignment access
                        .requestMatchers("/api/assignments/**")
                                .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        // Assignment submission access
                        .requestMatchers("/api/submissions/**")
                                .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        // Catch-all MUST be last
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}