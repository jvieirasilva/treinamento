package com.api.treinamento.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService - Testes Unitários")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L;        // 24h
    private static final long REFRESH_EXPIRATION = 604800000L; // 7d

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);

        userDetails = User.builder()
                .username("user@teste.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    // ─── generateToken ────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken: deve gerar token não nulo")
    void generateToken_deveGerarTokenNaoNulo() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateToken: token deve conter 3 partes separadas por ponto")
    void generateToken_tokenTemFormatoJwt() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token.split("\\.")).hasSize(3);
    }

    // ─── extractUsername ──────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername: deve extrair email correto do token")
    void extractUsername_deveRetornarEmailCorreto() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("user@teste.com");
    }

    // ─── isTokenValid ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid: token válido para o mesmo utilizador deve retornar true")
    void isTokenValid_tokenValidoParaMesmoUser_retornaTrue() {
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: token de outro utilizador deve retornar false")
    void isTokenValid_tokenDeOutroUser_retornaFalse() {
        String token = jwtService.generateToken(userDetails);

        UserDetails outroUser = User.builder()
                .username("outro@teste.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean valid = jwtService.isTokenValid(token, outroUser);

        assertThat(valid).isFalse();
    }

    // ─── generateRefreshToken ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateRefreshToken: deve gerar refresh token não nulo")
    void generateRefreshToken_deveGerarTokenNaoNulo() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertThat(refreshToken).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateRefreshToken: refresh token deve ter utilizador correto")
    void generateRefreshToken_deveTerUsernameCorreto() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        String username = jwtService.extractUsername(refreshToken);

        assertThat(username).isEqualTo("user@teste.com");
    }

    @Test
    @DisplayName("generateRefreshToken: refresh token deve ser diferente do access token")
    void generateRefreshToken_diferenteDoAccessToken() {
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }
}
