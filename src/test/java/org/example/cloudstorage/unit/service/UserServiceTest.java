package org.example.cloudstorage.unit.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.request.AuthUserRequest;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - unit тестирование")
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    SecurityContextRepository securityContextRepository;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpServletResponse httpServletResponse;

    @InjectMocks
    UserService userService;

    private AuthUserRequest request;
    private User user;


    @BeforeEach
    void setUp() {
        // Подготовка данных
        request = new AuthUserRequest();
        request.setUsername("john");
        request.setPassword("password123");

        user = new User();
        user.setUsername("john");
        user.setPassword("$2a$10$hashedPassword");
    }


    @Test
    @DisplayName("Успешаная авторизация - возвращает UserDTO")
    void login_ValidCredentials_Success() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mock(Authentication.class));

        UserDTO response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("username");

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());


        verify(securityContextRepository).saveContext(
                any(),
                eq(httpServletRequest),
                eq(httpServletResponse)
        );

    }

    @Test
    @DisplayName("Пользователь не найден, Ошибка Bad Credentional")
    void login_UserNotFound_BadCredentials() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Username not correct");
    }

    @Test
    @DisplayName("Успешная выдача токен(сессии) - возвращает void")
    void login_AccessSessionForUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);


        UserDTO response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john");

        verify(securityContextRepository, times(1)).saveContext(
                any(SecurityContext.class),
                eq(httpServletRequest),
                eq(httpServletResponse)
        );

        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));

    }
}