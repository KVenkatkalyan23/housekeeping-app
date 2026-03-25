package com.ibe.housekeeping.auth.service;

import com.ibe.housekeeping.auth.dto.CreateUserRequest;
import com.ibe.housekeeping.auth.dto.CreateUserResponse;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.dto.LoginResponse;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.auth.security.JwtService;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        Role role = request.role() == null ? Role.STAFF : request.role();

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        return new CreateUserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }
}
