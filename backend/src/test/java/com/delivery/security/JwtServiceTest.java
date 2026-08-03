package com.delivery.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "delivery-tracking-secret-key-change-in-production-256bits-min";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
    }

    @Test
    @DisplayName("deve gerar token e extrair o e-mail do subject")
    void generateAndExtractEmail() {
        String token = jwtService.generateToken("ana@email.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("ana@email.com");
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("deve invalidar token adulterado")
    void isValid_tamperedToken() {
        String token = jwtService.generateToken("ana@email.com");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("deve invalidar token com formato inválido")
    void isValid_malformedToken() {
        assertThat(jwtService.isValid("nao-e-um-jwt")).isFalse();
    }

    @Test
    @DisplayName("deve invalidar token expirado")
    void isValid_expiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L);
        String token = shortLived.generateToken("ana@email.com");

        Thread.sleep(20);

        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("não deve validar token assinado com outra secret")
    void isValid_differentSecret() {
        String token = jwtService.generateToken("ana@email.com");
        JwtService otherService = new JwtService(
                "outra-secret-bem-diferente-com-tamanho-suficiente-256bits",
                3_600_000L);

        assertThat(otherService.isValid(token)).isFalse();
    }
}
