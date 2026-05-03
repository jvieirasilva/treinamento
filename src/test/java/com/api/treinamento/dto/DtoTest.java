package com.api.treinamento.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DTOs - Testes Unitários")
class DtoTest {

    // ─── ApiResponse ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("ApiResponse.sucesso: deve criar resposta de sucesso")
    void apiResponse_sucesso_deveCriarCorretamente() {
        ApiResponse<String> response = ApiResponse.sucesso("Operação realizada", "dados");

        assertThat(response.isSucesso()).isTrue();
        assertThat(response.getMensagem()).isEqualTo("Operação realizada");
        assertThat(response.getDados()).isEqualTo("dados");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("ApiResponse.erro: deve criar resposta de erro")
    void apiResponse_erro_deveCriarCorretamente() {
        ApiResponse<Void> response = ApiResponse.erro("Erro ao processar");

        assertThat(response.isSucesso()).isFalse();
        assertThat(response.getMensagem()).isEqualTo("Erro ao processar");
        assertThat(response.getDados()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("ApiResponse.sucesso: deve aceitar dados nulos")
    void apiResponse_sucesso_dadosNulos() {
        ApiResponse<Void> response = ApiResponse.sucesso("OK", null);

        assertThat(response.isSucesso()).isTrue();
        assertThat(response.getDados()).isNull();
    }

    @Test
    @DisplayName("ApiResponse: builder deve funcionar corretamente")
    void apiResponse_builder_deveFuncionar() {
        LocalDateTime now = LocalDateTime.now();
        ApiResponse<Integer> response = ApiResponse.<Integer>builder()
                .sucesso(true)
                .mensagem("Teste")
                .dados(42)
                .timestamp(now)
                .build();

        assertThat(response.getDados()).isEqualTo(42);
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

    // ─── FornecedorRequest ────────────────────────────────────────────────────

    @Test
    @DisplayName("FornecedorRequest: builder deve criar objeto completo")
    void fornecedorRequest_builder_deveCriarCompleto() {
        FornecedorRequest req = FornecedorRequest.builder()
                .nome("Tech Ltda")
                .cnpj("11.222.333/0001-44")
                .email("tech@tech.com")
                .telefone("(11) 1111-1111")
                .logradouro("Av. Paulista")
                .numero("100")
                .complemento("Sala 1")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .cep("01310-100")
                .build();

        assertThat(req.getNome()).isEqualTo("Tech Ltda");
        assertThat(req.getCnpj()).isEqualTo("11.222.333/0001-44");
        assertThat(req.getEmail()).isEqualTo("tech@tech.com");
        assertThat(req.getEstado()).isEqualTo("SP");
        assertThat(req.getComplemento()).isEqualTo("Sala 1");
    }

    @Test
    @DisplayName("FornecedorRequest: setter deve alterar campos")
    void fornecedorRequest_setter_deveAlterarCampos() {
        FornecedorRequest req = new FornecedorRequest();
        req.setNome("Novo Nome");
        req.setEstado("RJ");

        assertThat(req.getNome()).isEqualTo("Novo Nome");
        assertThat(req.getEstado()).isEqualTo("RJ");
    }

    // ─── FornecedorResponse ───────────────────────────────────────────────────

    @Test
    @DisplayName("FornecedorResponse: builder deve criar objeto completo")
    void fornecedorResponse_builder_deveCriarCompleto() {
        LocalDateTime now = LocalDateTime.now();
        FornecedorResponse res = FornecedorResponse.builder()
                .id(1L)
                .nome("Tech Ltda")
                .cnpj("11.222.333/0001-44")
                .email("tech@tech.com")
                .telefone("(11) 1111-1111")
                .logradouro("Av. Paulista")
                .numero("100")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .cep("01310-100")
                .ativo(true)
                .criadoEm(now)
                .atualizadoEm(now)
                .build();

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getNome()).isEqualTo("Tech Ltda");
        assertThat(res.getAtivo()).isTrue();
        assertThat(res.getCriadoEm()).isEqualTo(now);
        assertThat(res.getAtualizadoEm()).isEqualTo(now);
    }

    @Test
    @DisplayName("FornecedorResponse: enderecoCompleto deve ser configurável")
    void fornecedorResponse_enderecoCompleto_deveSerConfiguravel() {
        FornecedorResponse res = FornecedorResponse.builder()
                .id(1L)
                .enderecoCompleto("Av. Paulista, 100, Sala 1 - Bela Vista, São Paulo/SP - 01310-100")
                .build();

        assertThat(res.getEnderecoCompleto())
                .contains("Av. Paulista")
                .contains("São Paulo");
    }

    // ─── UserDTO ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserDTO: builder deve criar objeto completo")
    void userDTO_builder_deveCriarCompleto() {
        UserDTO dto = UserDTO.builder()
                .id(1L)
                .fullName("João Silva")
                .email("joao@teste.com")
                .role("ADMIN")
                .isActive(true)
                .isNotLocked(true)
                .isChangePassword(false)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("João Silva");
        assertThat(dto.getEmail()).isEqualTo("joao@teste.com");
        assertThat(dto.getRole()).isEqualTo("ADMIN");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.isNotLocked()).isTrue();
        assertThat(dto.isChangePassword()).isFalse();
    }

    @Test
    @DisplayName("RegisterRequest: deve criar objeto via setter")
    void registerRequest_setter_deveFuncionar() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("João Silva");
        req.setEmail("joao@teste.com");
        req.setPassword("senha123");
        req.setRole("ADMIN");
        req.setIsActive(true);
        req.setIsNotLocked(true);
        req.setIsChangePassword(false);

        assertThat(req.getFullName()).isEqualTo("João Silva");
        assertThat(req.getEmail()).isEqualTo("joao@teste.com");
        assertThat(req.getRole()).isEqualTo("ADMIN");
        assertThat(req.getIsActive()).isTrue();
    }
}
