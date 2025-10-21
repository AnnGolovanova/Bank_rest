package com.example.bankcards.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtUtil;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;

    @MockBean UserRepository users;
    @MockBean UserService userService;
    @MockBean PasswordEncoder encoder;
    @MockBean JwtUtil jwt;


    private static String json(String u, String p) {
        return "{\"username\":\"" + u + "\",\"password\":\"" + p + "\"}";
    }

    private static User user(String username, String hash, Set<Role> roles) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(hash);
        u.setRoles(roles);
        return u;
    }

    @Test
    @DisplayName("POST /auth/login — 200 OK при валидных данных")
    void login_ok() throws Exception {
        var u = user("alice", "{bcrypt}hash", Set.of(Role.USER));
        when(users.findByUsername("alice")).thenReturn(Optional.of(u));
        when(encoder.matches("pass", "{bcrypt}hash")).thenReturn(true);
        when(jwt.generateToken(eq("alice"), ArgumentMatchers.anySet())).thenReturn("JWT_TOKEN");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("alice","pass")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken", notNullValue()));

        verify(users).findByUsername("alice");
        verify(encoder).matches("pass", "{bcrypt}hash");
        verify(jwt).generateToken(eq("alice"), anySet());
    }

    @Test
    @DisplayName("POST /auth/login — 401 если пользователь не найден")
    void login_unauthorized_whenUserNotFound() throws Exception {
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("ghost","any")))
                .andExpect(status().isUnauthorized());

        verify(users).findByUsername("ghost");
        verifyNoInteractions(encoder, jwt);
    }

    @Test
    @DisplayName("POST /auth/login — 401 если пароль не совпал")
    void login_unauthorized_whenPasswordMismatch() throws Exception {
        var u = user("bob", "{bcrypt}hash", Set.of(Role.USER));
        when(users.findByUsername("bob")).thenReturn(Optional.of(u));
        when(encoder.matches("bad", "{bcrypt}hash")).thenReturn(false);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("bob","bad")))
                .andExpect(status().isUnauthorized());


        verify(users).findByUsername("bob");
        verify(encoder).matches("bad", "{bcrypt}hash");
        verifyNoInteractions(jwt);
    }

    @Test
    @DisplayName("POST /auth/signup — 200 OK и вызов userService.createUser")
    void signup_ok() throws Exception {
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("newuser","secret")))
                .andExpect(status().isOk());

        verify(userService).createUser(eq("newuser"), eq("secret"), eq(Set.of(Role.USER)));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("POST /auth/login — 400 при неверном JSON (валидация @Valid)")
    void login_badRequest_onInvalidPayload() throws Exception {
        // пустой username (если в AuthRequest стоит @NotBlank)
        String bad = "{\"username\":\"\",\"password\":\"pass\"}";

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bad))
                .andExpect(status().isBadRequest());
    }
}