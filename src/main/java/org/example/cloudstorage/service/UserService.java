package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.dto.UserDTO;
import org.example.cloudstorage.model.exception.UsernameExistsException;
import org.example.cloudstorage.model.request.AuthUserRequest;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
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


    public UserDTO login(
            AuthUserRequest authUserRequest
    ) {
        User user = userRepository
                .findByUsername(authUserRequest.getUsername()).orElseThrow(() ->
                        new BadCredentialsException("Username not correct"));

        if (!passwordEncoder.matches(authUserRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password not correct");
        }

        return new UserDTO(authUserRequest.getUsername());


    }


    public UserDTO register(
            AuthUserRequest user
    ) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameExistsException("Username already exists: %s".formatted(user.getUsername()));
        }

        User userEntity = new User();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(userEntity);


        return new UserDTO(user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return new org.example.cloudstorage.security.UserDetails(user);
    }
}
