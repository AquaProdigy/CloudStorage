package org.example.cloudstorage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.request.UserRegisterRequest;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${auth}")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("${auth.register}")
    public ResponseEntity<UserDTO> register(@RequestBody @Valid UserRegisterRequest user) {
        UserDTO userDTO = userService.register(user);

        return ResponseEntity.ok(userDTO);
    }
}
