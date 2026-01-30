package org.example.cloudstorage.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "user")
@RestController
@RequestMapping("${api}")
public class UserController {

    @Tag(name = "user_me", description = "Get auth user")
    @GetMapping("${user.me}")
    public ResponseEntity<UserDTO> getUser(
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(new UserDTO(authentication.getName()));

    }
}
