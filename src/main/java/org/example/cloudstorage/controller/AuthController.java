package org.example.cloudstorage.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.exception.AlreadyAuthenticatedException;
import org.example.cloudstorage.model.exception.UnauthorizedException;
import org.example.cloudstorage.model.request.AuthUserRequest;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("${api}")
@Tag(name = "Auth controller")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;


    private void authenticateUser(
            AuthUserRequest userRegisterRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userRegisterRequest.getUsername(), userRegisterRequest.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

    }

    @PostMapping("${auth.register}")
    public ResponseEntity<UserDTO> register(
            @RequestBody @Valid AuthUserRequest userRegisterRequest,
            HttpServletRequest request,
            HttpServletResponse response

    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            throw new AlreadyAuthenticatedException(ApiErrors.USER_ALREADY_AUTHENTICATED.getMessage());
        }

        log.info("Registering request for username: {}", userRegisterRequest.getUsername());

        UserDTO userDTO = userService.register(userRegisterRequest);
        authenticateUser(userRegisterRequest, request, response);

        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @PostMapping("${auth.login}")
    public ResponseEntity<UserDTO> login(
            @RequestBody @Valid AuthUserRequest authUserRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            throw new AlreadyAuthenticatedException(ApiErrors.USER_ALREADY_AUTHENTICATED.getMessage());
        }

        log.info("Login request for username: {}", authUserRequest.getUsername());

        UserDTO userDTO = userService.login(authUserRequest);
        authenticateUser(authUserRequest, request, response);
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);

    }

    @PostMapping("${auth.logout}")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        new SecurityContextLogoutHandler().logout(request, response, null);
        return ResponseEntity.noContent().build();
    }

}
