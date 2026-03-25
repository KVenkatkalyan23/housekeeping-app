package com.ibe.housekeeping.auth.controller;

import com.ibe.housekeeping.auth.dto.CreateUserRequest;
import com.ibe.housekeeping.auth.dto.CreateUserResponse;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.dto.LoginResponse;
import com.ibe.housekeeping.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return authService.createUser(request);
    }
}
