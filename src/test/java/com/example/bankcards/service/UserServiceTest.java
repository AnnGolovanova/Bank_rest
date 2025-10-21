package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

class UserServiceTest {

    UserRepository users;
    PasswordEncoder encoder;
    UserService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserService(users, encoder);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrent: возвращает пользователя из репозитория по имени из SecurityContext")
    void getCurrent_ok() {

        var auth = new UsernamePasswordAuthenticationToken("alice", "N/A");
        SecurityContextHolder.getContext().setAuthentication(auth);

        var u = User.builder().id(1L).username("alice").build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(u));

        User current = service.getCurrent();

        assertNotNull(current);
        assertEquals("alice", current.getUsername());
        verify(users).findByUsername("alice");
    }

    @Test
    @DisplayName("getCurrent: если нет аутентификации — IllegalStateException('no auth')")
    void getCurrent_noAuth_throws() {
        SecurityContextHolder.clearContext(); // нет аутентификации
        var ex = assertThrows(IllegalStateException.class, () -> service.getCurrent());
        assertEquals("no auth", ex.getMessage());
        verifyNoInteractions(users);
    }

    @Test
    @DisplayName("getCurrent: если пользователь не найден — NoSuchElementException")
    void getCurrent_userNotFound_throws() {
        var auth = new UsernamePasswordAuthenticationToken("bob", "N/A");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(users.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getCurrent());
        verify(users).findByUsername("bob");
    }

    @Test
    @DisplayName("createUser: хеширует пароль, сохраняет пользователя и возвращает сохранённого")
    void createUser_ok() {
        when(encoder.encode("plain")).thenReturn("{bcrypt}hash");

        when(users.save(any(User.class))).thenAnswer(inv -> {
            User toSave = inv.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        Set<Role> roles = Set.of(Role.USER);
        User saved = service.createUser("newuser", "plain", roles);

        assertNotNull(saved.getId());
        assertEquals(10L, saved.getId());
        assertEquals("newuser", saved.getUsername());
        assertEquals("{bcrypt}hash", saved.getPasswordHash());
        assertEquals(roles, saved.getRoles());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User passed = captor.getValue();
        assertEquals("newuser", passed.getUsername());
        assertEquals("{bcrypt}hash", passed.getPasswordHash());
        assertEquals(roles, passed.getRoles());

        verify(encoder).encode("plain");
    }
}