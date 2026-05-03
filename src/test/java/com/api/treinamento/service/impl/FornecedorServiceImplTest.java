package com.api.treinamento.service.impl;
import com.api.treinamento.dto.FornecedorRequest;
import com.api.treinamento.dto.FornecedorResponse;
import com.api.treinamento.entity.Fornecedor;
import com.api.treinamento.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FornecedorServiceImpl - Testes Unitários")
class FornecedorServiceImplTest {

    @Mock
    private FornecedorRepository repository;

    @InjectMocks
    private FornecedorServiceImpl service;

    private FornecedorRequest request;
    private Fornecedor fornecedor;

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

        fornecedor = Fornecedor.builder()
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

    // ─── criar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criar: deve criar fornecedor com sucesso")
    void criar_sucesso() {
        when(repository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(repository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        FornecedorResponse response = service.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Tech Solutions Ltda");
        assertThat(response.getCnpj()).isEqualTo("11.222.333/0001-44");
        assertThat(response.getEstado()).isEqualTo("SP");
        verify(repository).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("criar: deve lançar exceção quando CNPJ já existe")
    void criar_cnpjDuplicado_lancaExcecao() {
        when(repository.existsByCnpj(request.getCnpj())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um fornecedor cadastrado com o CNPJ");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("criar: deve converter estado para maiúsculas")
    void criar_estadoConvertidoParaMaiusculas() {
        request.setEstado("sp");
        when(repository.existsByCnpj(any())).thenReturn(false);
        when(repository.save(any(Fornecedor.class))).thenAnswer(inv -> {
            Fornecedor f = inv.getArgument(0);
            assertThat(f.getEstado()).isEqualTo("SP");
            return fornecedor;
        });

        service.criar(request);

        verify(repository).save(argThat(f -> "SP".equals(f.getEstado())));
    }

    @Test
    @DisplayName("criar: deve definir ativo=true por padrão")
    void criar_ativoVerdadeiroPorPadrao() {
        when(repository.existsByCnpj(any())).thenReturn(false);
        when(repository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        service.criar(request);

        verify(repository).save(argThat(f -> Boolean.TRUE.equals(f.getAtivo())));
    }

    // ─── atualizar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("atualizar: deve atualizar fornecedor com sucesso")
    void atualizar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(repository.existsByCnpjAndIdNot(any(), anyLong())).thenReturn(false);
        when(repository.existsByEmailAndIdNot(any(), anyLong())).thenReturn(false);
        when(repository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        FornecedorResponse response = service.atualizar(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(repository).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando fornecedor não encontrado")
    void atualizar_fornecedorNaoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Fornecedor não encontrado com id: 99");
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando CNPJ pertence a outro fornecedor")
    void atualizar_cnpjDuplicado_lancaExcecao() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(repository.existsByCnpjAndIdNot(request.getCnpj(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um fornecedor cadastrado com o CNPJ");
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando email pertence a outro fornecedor")
    void atualizar_emailDuplicado_lancaExcecao() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(repository.existsByCnpjAndIdNot(any(), anyLong())).thenReturn(false);
        when(repository.existsByEmailAndIdNot(request.getEmail(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um fornecedor cadastrado com o e-mail");
    }

    // ─── deletar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletar: deve inativar fornecedor (soft delete)")
    void deletar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(repository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        service.deletar(1L);

        verify(repository).save(argThat(f -> Boolean.FALSE.equals(f.getAtivo())));
    }

    @Test
    @DisplayName("deletar: deve lançar exceção quando fornecedor não encontrado")
    void deletar_fornecedorNaoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Fornecedor não encontrado com id: 99");

        verify(repository, never()).save(any());
    }

    // ─── buscarPorId ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: deve retornar fornecedor quando encontrado")
    void buscarPorId_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));

        FornecedorResponse response = service.buscarPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Tech Solutions Ltda");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando não encontrado")
    void buscarPorId_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Fornecedor não encontrado com id: 99");
    }

    // ─── buscarPorNome ────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorNome: deve retornar página de fornecedores")
    void buscarPorNome_sucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Fornecedor> page = new PageImpl<>(List.of(fornecedor));
        when(repository.findByNomeContainingIgnoreCase("Tech", pageable)).thenReturn(page);

        Page<FornecedorResponse> resultado = service.buscarPorNome("Tech", pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Tech Solutions Ltda");
    }

    @Test
    @DisplayName("buscarPorNome: deve lançar exceção quando nome é vazio")
    void buscarPorNome_nomeVazio_lancaExcecao() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> service.buscarPorNome("", pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O parâmetro 'nome' não pode ser vazio");
    }

    @Test
    @DisplayName("buscarPorNome: deve lançar exceção quando nome é nulo")
    void buscarPorNome_nomeNulo_lancaExcecao() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> service.buscarPorNome(null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O parâmetro 'nome' não pode ser vazio");
    }

    // ─── listarTodos ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodos: deve retornar página de fornecedores ativos")
    void listarTodos_sucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Fornecedor> page = new PageImpl<>(List.of(fornecedor));
        when(repository.findAllAtivos(pageable)).thenReturn(page);

        Page<FornecedorResponse> resultado = service.listarTodos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getAtivo()).isTrue();
    }

    @Test
    @DisplayName("listarTodos: deve retornar página vazia quando não há fornecedores")
    void listarTodos_semFornecedores_retornaPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAllAtivos(pageable)).thenReturn(Page.empty());

        Page<FornecedorResponse> resultado = service.listarTodos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
    }

    // ─── enderecoCompleto ─────────────────────────────────────────────────────

    @Test
    @DisplayName("toResponse: deve montar enderecoCompleto corretamente com complemento")
    void toResponse_enderecoCompletoComComplemento() {
        when(repository.existsByCnpj(any())).thenReturn(false);
        when(repository.save(any())).thenReturn(fornecedor);

        FornecedorResponse response = service.criar(request);

        assertThat(response.getEnderecoCompleto())
                .contains("Av. Paulista")
                .contains("1000")
                .contains("Andar 5")
                .contains("Bela Vista")
                .contains("São Paulo")
                .contains("SP")
                .contains("01310-100");
    }

    @Test
    @DisplayName("toResponse: deve montar enderecoCompleto corretamente sem complemento")
    void toResponse_enderecoCompletoSemComplemento() {
        fornecedor.setComplemento(null);
        when(repository.existsByCnpj(any())).thenReturn(false);
        when(repository.save(any())).thenReturn(fornecedor);

        FornecedorResponse response = service.criar(request);

        assertThat(response.getEnderecoCompleto())
                .contains("Av. Paulista")
                .doesNotContain("Andar 5");
    }
}
