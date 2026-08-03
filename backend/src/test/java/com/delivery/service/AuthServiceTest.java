package com.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.delivery.dto.AuthResponse;
import com.delivery.dto.LoginRequest;
import com.delivery.dto.RegisterRequest;
import com.delivery.model.User;
import com.delivery.repository.UserRepository;
import com.delivery.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName(" Ana Silva ");
        registerRequest.setEmail(" Ana@Email.com ");
        registerRequest.setPassword("senha123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("ana@email.com");
        loginRequest.setPassword("senha123");
    }

    @Test
    @DisplayName("deve cadastrar usuário, persistir senha criptografada e retornar JWT")
    void register_success() {
        when(userRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken("ana@email.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getName()).isEqualTo("Ana Silva");
        assertThat(response.getEmail()).isEqualTo("ana@email.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Ana Silva");
        assertThat(saved.getEmail()).isEqualTo("ana@email.com");
        assertThat(saved.getPassword()).isEqualTo("hash-bcrypt");
    }

    @Test
    @DisplayName("deve rejeitar cadastro quando e-mail já existe")
    void register_emailAlreadyExists() {
        when(userRepository.existsByEmail("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusException = (ResponseStatusException) ex;
                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(statusException.getReason()).isEqualTo("E-mail já cadastrado");
                });

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("deve autenticar usuário com credenciais válidas e retornar JWT")
    void login_success() {
        User user = new User();
        user.setId(1L);
        user.setName("Ana Silva");
        user.setEmail("ana@email.com");
        user.setPassword("hash-bcrypt");

        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash-bcrypt")).thenReturn(true);
        when(jwtService.generateToken("ana@email.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getName()).isEqualTo("Ana Silva");
        assertThat(response.getEmail()).isEqualTo("ana@email.com");
    }

    @Test
    @DisplayName("deve rejeitar login quando usuário não existe")
    void login_userNotFound() {
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusException = (ResponseStatusException) ex;
                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(statusException.getReason()).isEqualTo("Credenciais inválidas");
                });

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("deve rejeitar login quando senha está incorreta")
    void login_invalidPassword() {
        User user = new User();
        user.setEmail("ana@email.com");
        user.setPassword("hash-bcrypt");

        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash-bcrypt")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusException = (ResponseStatusException) ex;
                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(statusException.getReason()).isEqualTo("Credenciais inválidas");
                });

        verify(jwtService, never()).generateToken(anyString());
    }
}
