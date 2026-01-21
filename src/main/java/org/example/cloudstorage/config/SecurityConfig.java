package org.example.cloudstorage.config;


import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudstorage.model.response.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(CsrfConfigurer::disable)
//                .formLogin(FormLoginConfigurer::disable)
//                .authorizeHttpRequests(auth ->
//                        auth
//                                .requestMatchers("/api/auth/sign-up", "/api/auth/sign-in").permitAll()
//                                .anyRequest().authenticated())
//                                .exceptionHandling(exception -> exception
//                                        .authenticationEntryPoint((request, response, authException) -> {
//                                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                                            response.setContentType("application/json");
//                                            response.setCharacterEncoding("UTF-8");
//                                            response.getWriter().write(new ErrorResponse("User unauthorized").toString());
//                                        })
//                                );
//        return http.build();
        http.csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll());
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authCfg) throws Exception {
        return authCfg.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
