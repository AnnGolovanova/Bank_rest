package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.TokenResponse;
import com.example.bankcards.security.jwt.JwtUtil;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;

import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest req) {
        User u = users.findByUsername(req.username()).orElse(null);
        if (u == null || !encoder.matches(req.password(), u.getPasswordHash()))
            return ResponseEntity.status(401).build();
        String token = jwt.generateToken(u.getUsername(), u.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
        return ResponseEntity.ok(new TokenResponse(token));
    }


    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody AuthRequest req){
        userService.createUser(req.username(), req.password(), Set.of(Role.USER));
        return ResponseEntity.ok().build();
    }
}
