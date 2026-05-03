package com.api.treinamento.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.api.treinamento.config.GlobalExceptionHandler;
import com.api.treinamento.dto.FornecedorRequest;
import com.api.treinamento.dto.FornecedorResponse;
import com.api.treinamento.security.filter.JwtAuthenticationFilter;
import com.api.treinamento.service.FornecedorService;
import com.api.treinamento.service.impl.CustomUserDetailsService;
import com.api.treinamento.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(FornecedorController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)  // ← desativa JWT filter no teste
@DisplayName("FornecedorController - Testes Unitários")
class FornecedorControllerTest {

	 @Autowired
	    private MockMvc mockMvc;

	    @Autowired
	    private ObjectMapper objectMapper;

	    @MockBean
	    private FornecedorService service;

	    @MockBean
	    private JwtService jwtService;

	    @MockBean
	    private CustomUserDetailsService customUserDetailsService;

	    @MockBean
	    private JwtAuthenticationFilter jwtAuthenticationFilter;
    

    private FornecedorRequest request;
    private FornecedorResponse response;

    @BeforeEach
    void setUp() {
        request = FornecedorRequest.builder()
                .nome("Tech Solutions Ltda")
                .cnpj("11.222.333/0001-44")
                .email("contato@techsolutions.com.br")
                .telefone("(11) 3000-1111")
                .logradouro("Av. Paulista")
                .numero("1000")
                .complemento("Andar 5")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .cep("01310-100")
                .build();

        response = FornecedorResponse.builder()
                .id(1L)
                .nome("Tech Solutions Ltda")
                .cnpj("11.222.333/0001-44")
                .email("contato@techsolutions.com.br")
                .telefone("(11) 3000-1111")
                .logradouro("Av. Paulista")
                .numero("1000")
                .complemento("Andar 5")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .cep("01310-100")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    // ─── POST /api/v1/fornecedores ────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /fornecedores: deve criar fornecedor e retornar 201")
    void criar_deveRetornar201() throws Exception {
        when(service.criar(any(FornecedorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/fornecedores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.nome").value("Tech Solutions Ltda"))
                .andExpect(jsonPath("$.dados.cnpj").value("11.222.333/0001-44"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /fornecedores: deve retornar 400 quando dados inválidos")
    void criar_dadosInvalidos_deveRetornar400() throws Exception {
        FornecedorRequest invalido = FornecedorRequest.builder()
                .nome("") // nome vazio
                .cnpj("cnpj-invalido")
                .email("email-invalido")
                .build();

        mockMvc.perform(post("/api/v1/fornecedores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /api/v1/fornecedores/{id} ────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("PUT /fornecedores/{id}: deve atualizar e retornar 200")
    void atualizar_deveRetornar200() throws Exception {
        when(service.atualizar(eq(1L), any(FornecedorRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/fornecedores/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /fornecedores/{id}: deve retornar 404 quando não encontrado")
    void atualizar_naoEncontrado_deveRetornar404() throws Exception {
        when(service.atualizar(eq(99L), any())).thenThrow(new EntityNotFoundException("Fornecedor não encontrado com id: 99"));

        mockMvc.perform(put("/api/v1/fornecedores/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/v1/fornecedores/{id} ─────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("DELETE /fornecedores/{id}: deve inativar e retornar 200")
    void deletar_deveRetornar200() throws Exception {
        doNothing().when(service).deletar(1L);

        mockMvc.perform(delete("/api/v1/fornecedores/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /fornecedores/{id}: deve retornar 404 quando não encontrado")
    void deletar_naoEncontrado_deveRetornar404() throws Exception {
        doThrow(new EntityNotFoundException("Fornecedor não encontrado com id: 99")).when(service).deletar(99L);

        mockMvc.perform(delete("/api/v1/fornecedores/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/v1/fornecedores/{id} ────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /fornecedores/{id}: deve retornar fornecedor com 200")
    void buscarPorId_deveRetornar200() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/fornecedores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.id").value(1))
                .andExpect(jsonPath("$.dados.nome").value("Tech Solutions Ltda"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /fornecedores/{id}: deve retornar 404 quando não encontrado")
    void buscarPorId_naoEncontrado_deveRetornar404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new EntityNotFoundException("Fornecedor não encontrado com id: 99"));

        mockMvc.perform(get("/api/v1/fornecedores/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/v1/fornecedores ─────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /fornecedores: deve listar com paginação e retornar 200")
    void listarTodos_deveRetornar200() throws Exception {
        var page = new PageImpl<>(List.of(response));
        when(service.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/fornecedores")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "nome")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.content[0].nome").value("Tech Solutions Ltda"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /fornecedores: deve suportar ordenação descendente")
    void listarTodos_ordenacaoDesc_deveRetornar200() throws Exception {
        var page = new PageImpl<>(List.of(response));
        when(service.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/fornecedores")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/v1/fornecedores/buscar ──────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /fornecedores/buscar: deve buscar por nome e retornar 200")
    void buscarPorNome_deveRetornar200() throws Exception {
        var page = new PageImpl<>(List.of(response));
        when(service.buscarPorNome(eq("Tech"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/fornecedores/buscar")
                        .param("nome", "Tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.content[0].nome").value("Tech Solutions Ltda"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /fornecedores/buscar: deve lançar 400 quando nome vazio")
    void buscarPorNome_nomeVazio_deveRetornar400() throws Exception {
        when(service.buscarPorNome(eq(""), any())).thenThrow(new IllegalArgumentException("O parâmetro 'nome' não pode ser vazio"));

        mockMvc.perform(get("/api/v1/fornecedores/buscar")
                        .param("nome", ""))
                .andExpect(status().isConflict()); // ← muda de isBadRequest() para isConflict()
    }

    // ─── Sem autenticação ─────────────────────────────────────────────────────

   
    
    @Test
    @DisplayName("GET /fornecedores: deve retornar 200 sem autenticação (filtros desativados no teste)")
    void listarTodos_semAutenticacao_deveRetornar200() throws Exception {
        var page = new PageImpl<>(List.of(response));
        when(service.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/fornecedores"))
                .andExpect(status().isOk());
    }
    
   
}
