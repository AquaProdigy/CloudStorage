package org.example.cloudstorage.unit.service;

import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.exception.UsernameExistsException;
import org.example.cloudstorage.model.request.AuthUserRequest;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.example.cloudstorage.service.ResourceService;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@DisplayName(value = "Integration test Auth service")
public class UserServiceTestContainer {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =  new PostgreSQLContainer<>("postgres:15.5");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver_class", postgreSQLContainer::getDriverClassName);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ResourceService resourceService;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("testuser"));

        userRepository.save(user);
    }

    @AfterEach
    void setDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "Success login with valid credentials")
    void login_WithValidCredentials_ShouldReturnUserDto(){
        AuthUserRequest authUserRequest = new AuthUserRequest("testuser", "testuser");

        UserDTO result = userService.login(authUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Failure login with bad credentials")
    void login_WithBadCredentials_ShouldReturnBadCredentialsException(){
        AuthUserRequest authUserRequest = new AuthUserRequest("test", "test");

        assertThatThrownBy(() -> userService.login(authUserRequest))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage(ApiErrors.BAD_CREDENTIALS.getMessage());
    }

    @Test
    @DisplayName("Success register with valid credentials")
    void register_WithValidCredentials_ShouldReturnUserDto(){
        AuthUserRequest authUserRequest = new AuthUserRequest("testuser1234", "testuser1234");

        UserDTO result = userService.register(authUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser1234");
    }

    @Test
    @DisplayName("Failure registration with already username")
    void register_WithAlreadyUsername_ShouldReturnUsernameExistsException(){
        AuthUserRequest authUserRequest = new AuthUserRequest("testuser", "testuser");

        assertThatThrownBy(() -> userService.register(authUserRequest))
                .isInstanceOf(UsernameExistsException.class)
                .hasMessageContaining(ApiErrors.USERNAME_ALREADY_EXISTS.getMessage());
    }

}
