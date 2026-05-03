package com.api.treinamento.service.impl;

import com.api.treinamento.entity.Role;
import com.api.treinamento.entity.User;
import com.api.treinamento.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - Testes Unitários")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("João Silva")
                .email("joao@teste.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .isActive(true)
                .isNotLocked(true)
                .isChangePassword(false)
                .joinDate(new Date())
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername: deve retornar UserDetails quando utilizador existe")
    void loadUserByUsername_sucesso() {
        when(userRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("joao@teste.com");

        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("joao@teste.com");
        assertThat(details.getPassword()).isEqualTo("encodedPassword");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: deve lançar exceção quando utilizador não existe")
    void loadUserByUsername_naoEncontrado_lancaExcecao() {
        when(userRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("naoexiste@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: naoexiste@teste.com");
    }

    @Test
    @DisplayName("loadUserByUsername: utilizador inativo deve ter isEnabled=false")
    void loadUserByUsername_utilizadorInativo() {
        User userInativo = User.builder()
                .id(1L)
                .fullName("João Silva")
                .email("joao@teste.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .isActive(false)
                .isNotLocked(true)
                .isChangePassword(false)
                .joinDate(new Date())
                .build();
        when(userRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(userInativo));

        UserDetails details = service.loadUserByUsername("joao@teste.com");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername: utilizador bloqueado deve ter isAccountNonLocked=false")
    void loadUserByUsername_utilizadorBloqueado() {
        User userBloqueado = User.builder()
                .id(1L)
                .fullName("João Silva")
                .email("joao@teste.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .isActive(true)
                .isNotLocked(false)
                .isChangePassword(false)
                .joinDate(new Date())
                .build();
        when(userRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(userBloqueado));

        UserDetails details = service.loadUserByUsername("joao@teste.com");

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername: utilizador com role USER deve ter authority ROLE_USER")
    void loadUserByUsername_roleUser() {
        User userComRoleUser = User.builder()
                .id(1L)
                .email("joao@teste.com")
                .password("encodedPassword")
                .role(Role.USER)
                .isActive(true)
                .isNotLocked(true)
                .joinDate(new Date())
                .build();
        when(userRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(userComRoleUser));

        UserDetails details = service.loadUserByUsername("joao@teste.com");

        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }
}