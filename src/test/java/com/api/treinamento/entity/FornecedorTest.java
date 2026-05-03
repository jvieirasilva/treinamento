package com.api.treinamento.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Fornecedor Entity - Testes Unitários")
class FornecedorTest {

    @Test
    @DisplayName("prePersist: deve definir criadoEm e atualizadoEm automaticamente")
    void prePersist_deveDefinirDatas() {
        Fornecedor f = new Fornecedor();
        f.prePersist();

        assertThat(f.getCriadoEm()).isNotNull();
        assertThat(f.getAtualizadoEm()).isNotNull();
        assertThat(f.getCriadoEm()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("preUpdate: deve atualizar atualizadoEm")
    void preUpdate_deveAtualizarData() throws InterruptedException {
        Fornecedor f = new Fornecedor();
        f.prePersist();
        LocalDateTime antes = f.getAtualizadoEm();
        Thread.sleep(10);
        f.preUpdate();

        assertThat(f.getAtualizadoEm()).isAfterOrEqualTo(antes);
    }

    @Test
    @DisplayName("builder: deve criar fornecedor com todos os campos")
    void builder_deveCriarFornecedor() {
        Fornecedor f = Fornecedor.builder()
                .id(1L)
                .nome("Tech Ltda")
                .cnpj("11.222.333/0001-44")
                .email("tech@tech.com")
                .telefone("(11) 1111-1111")
                .logradouro("Rua A")
                .numero("10")
                .complemento("Sala 1")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .cep("01310-100")
                .ativo(true)
                .build();

        assertThat(f.getId()).isEqualTo(1L);
        assertThat(f.getNome()).isEqualTo("Tech Ltda");
        assertThat(f.getCnpj()).isEqualTo("11.222.333/0001-44");
        assertThat(f.getEmail()).isEqualTo("tech@tech.com");
        assertThat(f.getAtivo()).isTrue();
        assertThat(f.getEstado()).isEqualTo("SP");
        assertThat(f.getComplemento()).isEqualTo("Sala 1");
    }

    @Test
    @DisplayName("builder: ativo deve ser true por padrão")
    void builder_ativoVerdadeiroPorPadrao() {
        Fornecedor f = Fornecedor.builder()
                .nome("Tech Ltda")
                .build();

        assertThat(f.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("noArgsConstructor: deve criar fornecedor vazio")
    void noArgsConstructor_deveCriarFornecedorVazio() {
        Fornecedor f = new Fornecedor();
        assertThat(f).isNotNull();
        assertThat(f.getId()).isNull();
        assertThat(f.getNome()).isNull();
    }

    @Test
    @DisplayName("setter: deve alterar campos corretamente")
    void setter_deveAlterarCampos() {
        Fornecedor f = new Fornecedor();
        f.setNome("Novo Nome");
        f.setAtivo(false);
        f.setEstado("RJ");

        assertThat(f.getNome()).isEqualTo("Novo Nome");
        assertThat(f.getAtivo()).isFalse();
        assertThat(f.getEstado()).isEqualTo("RJ");
    }

    @Test
    @DisplayName("toString: deve retornar representação não nula")
    void toString_deveRetornarString() {
        Fornecedor f = Fornecedor.builder().nome("Tech Ltda").build();
        assertThat(f.toString()).isNotNull().contains("Tech Ltda");
    }

    @Test
    @DisplayName("equals e hashCode: dois fornecedores com mesmo id devem ser iguais")
    void equalsHashCode_mesmoid_devemSerIguais() {
        Fornecedor f1 = Fornecedor.builder().id(1L).nome("Tech A").cnpj("11.222.333/0001-44").build();
        Fornecedor f2 = Fornecedor.builder().id(1L).nome("Tech A").cnpj("11.222.333/0001-44").build();

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
    }
}
