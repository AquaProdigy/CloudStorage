package org.example.cloudstorage.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.exception.UnauthorizedException;
import org.example.cloudstorage.model.request.AuthUserRequest;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("${api}")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;


    @PostMapping("${auth.register}")
    public ResponseEntity<UserDTO> register(
            @RequestBody @Valid AuthUserRequest userRegisterRequest,
            HttpServletRequest request,
            HttpServletResponse response

    ) {
        log.info("Registering request for username: {}", userRegisterRequest.getUsername());

        userService.register(userRegisterRequest);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userRegisterRequest.getUsername(), userRegisterRequest.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);


        return new ResponseEntity<>(new UserDTO(userRegisterRequest.getUsername()), HttpStatus.CREATED);
    }


    @PostMapping("${auth.login}")
    public ResponseEntity<UserDTO> login(
            @RequestBody @Valid AuthUserRequest authUserRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Login request for username: {}", authUserRequest.getUsername());

        UserDTO userDTO = userService.login(authUserRequest);


        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                authUserRequest.getUsername(), authUserRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.status(HttpStatus.OK).body(userDTO);

    }

    @PostMapping("${auth.logout}")
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

}
