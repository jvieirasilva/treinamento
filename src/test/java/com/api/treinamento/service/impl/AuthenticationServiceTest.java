package com.api.treinamento.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.api.treinamento.dto.RegisterRequest;
import com.api.treinamento.dto.UserDTO;
import com.api.treinamento.entity.Role;
import com.api.treinamento.entity.User;
import com.api.treinamento.repository.UserRepository;
import com.api.treinamento.request.AuthenticationRequest;
import com.api.treinamento.response.AuthenticationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService - Testes Unitários")
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService service;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("João Silva");
        registerRequest.setEmail("joao@teste.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole("ADMIN");
        registerRequest.setIsActive(true);
        registerRequest.setIsNotLocked(true);
        registerRequest.setIsChangePassword(false);

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

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: deve registar utilizador com sucesso")
    void register_sucesso() throws IOException {
        when(repository.findByEmail("joao@teste.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthenticationResponse response = service.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("joao@teste.com");
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("register: deve lançar exceção quando email já existe")
    void register_emailDuplicado_lancaExcecao() {
        when(repository.findByEmail("joao@teste.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Este email já está cadastrado");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("register: deve usar role USER quando role é inválida")
    void register_roleInvalida_usaRoleUser() throws IOException {
        registerRequest.setRole("ROLE_INVALIDA");
        when(repository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        service.register(registerRequest);

        verify(repository).save(argThat(u -> u.getRole() == Role.USER));
    }

    @Test
    @DisplayName("register: deve usar role USER quando role é nula")
    void register_roleNula_usaRoleUser() throws IOException {
        registerRequest.setRole(null);
        when(repository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        service.register(registerRequest);

        verify(repository).save(argThat(u -> u.getRole() == Role.USER));
    }

    @Test
    @DisplayName("register: deve usar role ADMIN quando role é ADMIN")
    void register_roleAdmin_usaRoleAdmin() throws IOException {
        registerRequest.setRole("ADMIN");
        when(repository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        service.register(registerRequest);

        verify(repository).save(argThat(u -> u.getRole() == Role.ADMIN));
    }

    // ─── authenticate ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("authenticate: deve autenticar utilizador com sucesso")
    void authenticate_sucesso() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("joao@teste.com");
        authRequest.setPassword("senha123");

        when(repository.findByEmailWithCompany("joao@teste.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthenticationResponse response = service.authenticate(authRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("joao@teste.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("authenticate: deve lançar exceção quando utilizador não encontrado")
    void authenticate_utilizadorNaoEncontrado_lancaExcecao() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("naoexiste@teste.com");
        authRequest.setPassword("senha123");

        when(repository.findByEmailWithCompany("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(authRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na autenticação");
    }

    // ─── getUserById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById: deve retornar UserDTO quando encontrado")
    void getUserById_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO dto = service.getUserById(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("joao@teste.com");
        assertThat(dto.getFullName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("getUserById: deve lançar exceção quando não encontrado")
    void getUserById_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado com ID: 99");
    }

    // ─── deleteUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser: deve deletar utilizador com sucesso")
    void deleteUser_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        service.deleteUser(1L);

        verify(repository).delete(user);
    }

    @Test
    @DisplayName("deleteUser: deve lançar exceção quando utilizador não encontrado")
    void deleteUser_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado com ID: 99");

        verify(repository, never()).delete(any());
    }

    // ─── createDefaultUserIfNotExists ─────────────────────────────────────────

    @Test
    @DisplayName("createDefaultUserIfNotExists: deve retornar utilizador existente sem criar novo")
    void createDefaultUserIfNotExists_utilizadorExistente_naoSalva() {
        when(repository.findByEmail("joao@teste.com")).thenReturn(Optional.of(user));

        UserDTO dto = service.createDefaultUserIfNotExists("joao@teste.com", "senha", "João Silva");

        assertThat(dto).isNotNull();
        assertThat(dto.getEmail()).isEqualTo("joao@teste.com");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("createDefaultUserIfNotExists: deve criar novo utilizador quando não existe")
    void createDefaultUserIfNotExists_utilizadorNaoExiste_cria() {
        when(repository.findByEmail("novo@teste.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha")).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(user);

        UserDTO dto = service.createDefaultUserIfNotExists("novo@teste.com", "senha", "Novo User");

        verify(repository).save(any(User.class));
    }
    
    
 // ─── updateUser ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser: deve atualizar utilizador com sucesso")
    void updateUser_sucesso() throws IOException {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("João Atualizado");
        req.setEmail("joao@teste.com");
        req.setPassword("novaSenha");
        req.setRole("ADMIN");
        req.setIsActive(true);
        req.setIsNotLocked(true);
        req.setIsChangePassword(false);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("joao@teste.com")).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(user);

        UserDTO dto = service.updateUser(1L, req);

        assertThat(dto).isNotNull();
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser: deve lançar exceção quando utilizador não encontrado")
    void updateUser_naoEncontrado_lancaExcecao() {
        RegisterRequest req = new RegisterRequest();
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(99L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado com ID: 99");
    }

    @Test
    @DisplayName("updateUser: deve lançar exceção quando email já em uso por outro utilizador")
    void updateUser_emailEmUso_lancaExcecao() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("outro@teste.com");

        User outroUser = User.builder().id(2L).email("outro@teste.com").role(Role.USER).joinDate(new Date()).build();
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("outro@teste.com")).thenReturn(Optional.of(outroUser));

        assertThatThrownBy(() -> service.updateUser(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email já está em uso por outro usuário");
    }

    // ─── searchUsersByName ────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchUsersByName: deve retornar página de utilizadores")
    void searchUsersByName_sucesso() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<User> page = new org.springframework.data.domain.PageImpl<>(List.of(user));
        when(repository.searchByNameOrEmail("João", pageable)).thenReturn(page);

        org.springframework.data.domain.Page<UserDTO> resultado = service.searchUsersByName("João", pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getEmail()).isEqualTo("joao@teste.com");
    }

    @Test
    @DisplayName("searchUsersByName: deve retornar página vazia quando não há resultados")
    void searchUsersByName_semResultados() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        when(repository.searchByNameOrEmail("inexistente", pageable))
                .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<UserDTO> resultado = service.searchUsersByName("inexistente", pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
    }
    
}
