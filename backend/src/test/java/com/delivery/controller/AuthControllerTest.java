package com.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.dto.AuthResponse;
import com.delivery.dto.LoginRequest;
import com.delivery.dto.RegisterRequest;
import com.delivery.security.JwtAuthFilter;
import com.delivery.security.JwtService;
import com.delivery.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /api/auth/register deve retornar 201 com token")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ana");
        request.setEmail("ana@email.com");
        request.setPassword("senha123");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("jwt-token", "Ana", "ana@email.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register deve retornar 400 quando payload é inválido")
    void register_invalidPayload() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("email-invalido");
        request.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login deve retornar 200 com token")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("ana@email.com");
        request.setPassword("senha123");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt-token", "Ana", "ana@email.com"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login deve retornar 401 com credenciais inválidas")
    void login_unauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("ana@email.com");
        request.setPassword("errada");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
