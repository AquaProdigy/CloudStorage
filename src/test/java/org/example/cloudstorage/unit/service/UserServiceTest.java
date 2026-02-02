package org.example.cloudstorage.unit.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.exception.UsernameExistsException;
import org.example.cloudstorage.model.request.AuthUserRequest;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.example.cloudstorage.service.ResourceService;
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
    ResourceService resourceService;

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
    @DisplayName("Success login - return UserDTO")
    void login_ValidCredentials_Success() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        UserDTO response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john");

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());


    }

    @Test
    @DisplayName("User not found - return Bad Credential")
    void login_UserNotFound_BadCredentials() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining(ApiErrors.BAD_CREDENTIALS.getMessage());
    }

    @Test
    @DisplayName("Success registration - return UserDto")
    void register_ValidCredentials_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        UserDTO response = userService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john");
    }

    @Test
    @DisplayName("Register failed, username already exists - return UsernameAlreadyExistsException")
    void register_UserAlreadyExists_UsernameAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UsernameExistsException.class)
                .hasMessageContaining(ApiErrors.USERNAME_ALREADY_EXISTS.getMessage());
    }

}