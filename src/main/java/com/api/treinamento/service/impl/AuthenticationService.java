package com.api.treinamento.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.treinamento.entity.Role;
import com.api.treinamento.entity.User;
import com.api.treinamento.repository.UserRepository;
import com.api.treinamento.request.AuthenticationRequest;
import com.api.treinamento.response.AuthenticationResponse;
import com.api.treinamento.dto.RegisterRequest;
import com.api.treinamento.dto.UserDTO;

@Service
public class AuthenticationService {

	private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);
    
    public AuthenticationService(UserRepository repository, 
            PasswordEncoder passwordEncoder, 
            JwtService jwtService, 
            AuthenticationManager authenticationManager) {
					this.repository = repository;
					this.passwordEncoder = passwordEncoder;
					this.jwtService = jwtService;
					this.authenticationManager = authenticationManager;
			}

    public AuthenticationResponse register(RegisterRequest request) throws IOException {

        LOGGER.info("Registando utilizador: {}", request.getFullName());
        // ✅ VERIFICAR SE EMAIL JÁ EXISTE
        if (repository.findByEmail(request.getEmail()).isPresent()) {
        	LOGGER.error("Email já cadastrado: {}", request.getEmail());
            throw new RuntimeException("Este email já está cadastrado. Por favor, use outro email ou faça login.");
        }
        Role userRole = Role.USER; // default
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                userRole = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid role: {}. Using default USER role.", request.getRole());
                userRole = Role.USER;
            }
        }

        User user = User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(userRole)
            .joinDate(new Date())
            .isActive(true)
            .isNotLocked(true)
            .isChangePassword(request.getIsChangePassword())
            .build();
        repository.save(user);
        
       // String testToken = UUID.randomUUID().toString();
        
        //String newToken = createEmailVerificationToken(request.getEmail(),testToken);
        

        // Geração dos tokens JWT
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Criando o DTO do usuário
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                //.profileImageUrl(user.getProfileImageUrl())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginDateDisplay(user.getLastLoginDateDisplay())
                .joinDate(user.getJoinDate())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .isNotLocked(user.isNotLocked())
                .isChangePassword(user.isChangePassword())
                .build();

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            var user = repository.findByEmailWithCompany(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado ou Inactivo/Bloqueado"));

            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                   // .profileImageUrl(user.getProfileImageUrl())
                    .lastLoginDate(user.getLastLoginDate())
                    .lastLoginDateDisplay(user.getLastLoginDateDisplay())
                    .joinDate(user.getJoinDate())
                    .role(user.getRole().name())
                    .isActive(user.isActive())
                    .isNotLocked(user.isNotLocked())
                    .isChangePassword(user.isChangePassword())
                    .build();
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .user(userDTO)
                    .build();
        } catch (Exception e) {
        	 e.printStackTrace();
            throw new RuntimeException("Falha na autenticação", e);
        }
    }
    
    
    public UserDTO createDefaultUserIfNotExists(String email, String rawPassword, String fullName) {
        return repository.findByEmail(email)
                .map(user -> {
                    LOGGER.info("Usuário padrão já existe: {}", email);
                    return toDTO(user);
                })
                .orElseGet(() -> {
                    User user = User.builder()
                            .fullName(fullName)
                            .email(email)
                            .password(passwordEncoder.encode(rawPassword))
                            .role(Role.USER)
                            .isActive(true)
                            .isNotLocked(true)
                            .isChangePassword(false)
                            .joinDate(new Date())
                            .lastLoginDate(new Date())
                            .lastLoginDateDisplay(new Date())
                            .build();

                    repository.save(user);
                    LOGGER.info("Usuário padrão criado com sucesso: {}", email);
                    return toDTO(user);
                });
    }
    
    /**
     * Pesquisa usuários por nome ou email com paginação
     * 
     * @param searchTerm Termo de busca (nome ou email)
     * @param pageable Informações de paginação e ordenação
     * @return Página de UserDTO
     */
    public Page<UserDTO> searchUsersByName(String searchTerm, Pageable pageable) {
        LOGGER.info("Pesquisando usuários com termo: '{}', página: {}, tamanho: {}", 
                    searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> users = repository.searchByNameOrEmail(searchTerm, pageable);
        
        // Converter Page<User> para Page<UserDTO>
        Page<UserDTO> userDTOs = users.map(this::toDTO);
        
        LOGGER.info("Encontrados {} usuários na página {} de {}", 
                    userDTOs.getNumberOfElements(), 
                    userDTOs.getNumber() + 1, 
                    userDTOs.getTotalPages());
        
        return userDTOs;
    }
    
    /**
     * Busca usuário por ID
     */
    public UserDTO getUserById(Long id) { // ✅ ALTERADO DE Long PARA Integer
        LOGGER.info("Buscando usuário com ID: {}", id);
        
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        
        return toDTO(user);
    }
    
    /**
     * Atualiza um usuário existente
     */
    public UserDTO updateUser(Long id, RegisterRequest request) throws IOException { // ✅ ALTERADO DE Long PARA Integer
        LOGGER.info("Atualizando usuário com ID: {}", id);
        
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        
        // Atualizar campos apenas se fornecidos
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Verificar se o email já existe em outro usuário
            repository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new RuntimeException("Email já está em uso por outro usuário");
                }
            });
            user.setEmail(request.getEmail());
        }
        
        // Atualizar senha apenas se fornecida
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Atualizar role
        if (request.getRole() != null) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Role inválida: {}", request.getRole());
            }
        }
        
        // Atualizar flags
        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }
        
        if (request.getIsNotLocked() != null) {
            user.setNotLocked(request.getIsNotLocked());
        }
        
        if (request.getIsChangePassword() != null) {
            user.setChangePassword(request.getIsChangePassword());
        }
        
        
        user = repository.save(user);
        LOGGER.info("Usuário atualizado com sucesso: {}", id);
        
        return toDTO(user);
    }
    
    /**
     * Deleta um usuário (exclusão física)
     * Alternativamente, pode fazer exclusão lógica setando isActive = false
     */
    public void deleteUser(Long id) { // ✅ MUDAR DE Integer PARA Long
        LOGGER.info("Deletando usuário com ID: {}", id);
        
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        
      
        // Exclusão física
        repository.delete(user);
        
        LOGGER.info("Usuário deletado com sucesso: {}", id);
    }
    
 
    
    
    
    
    /**
     * Extrai o nome do arquivo da URL do S3
     */
    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // URL format: https://bucket.s3.region.amazonaws.com/profile-images/filename.png
        String[] parts = url.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        
        return null;
    }
    
   
    
    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                //.profileImageUrl(user.getProfileImageUrl())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginDateDisplay(user.getLastLoginDateDisplay())
                .joinDate(user.getJoinDate())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .isNotLocked(user.isNotLocked())
                .isChangePassword(user.isChangePassword())
                .build();
    }
    
    /**
     * Cria um token de verificação de email e salva no banco de dados
     * 
     * @param email Email do usuário
     * @return Token UUID gerado
     */
    private String createEmailVerificationToken(String email, String token) {
        LOGGER.info("Gerando token de verificação para: {}", email);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TimeUnit.HOURS.toMillis(24));
        
        LOGGER.info("Token salvo no banco de dados");
        LOGGER.info("Token: {}", token);
        LOGGER.info("Email: {}", email);
        LOGGER.info("Expira em: {}", expiryDate);
        
        return token;
    }
    


}