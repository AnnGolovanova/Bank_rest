package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public User getCurrent() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new IllegalStateException("no auth");
        return users.findByUsername(auth.getName()).orElseThrow();
    }

    public User createUser(String username, String rawPassword, Set<Role> roles) {
        User u = User.builder()
                .username(username)
                .passwordHash(encoder.encode(rawPassword))
                .roles(roles)
                .build();
        return users.save(u);
    }


}
