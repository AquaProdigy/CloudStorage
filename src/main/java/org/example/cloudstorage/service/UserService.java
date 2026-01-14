package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.exception.UsernameExistsException;
import org.example.cloudstorage.model.request.UserRegisterRequest;
import org.example.cloudstorage.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO register(UserRegisterRequest user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameExistsException(user.getUsername());
        }

        User userEntity = new User();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(userEntity);

        var userDetails = loadUserByUsername(user.getUsername());

        var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        return new UserDTO(user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return new org.example.cloudstorage.security.UserDetails(user);
    }
}
