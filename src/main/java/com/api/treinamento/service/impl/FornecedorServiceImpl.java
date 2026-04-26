package com.api.treinamento.service.impl;

import com.api.treinamento.dto.FornecedorRequest;
import com.api.treinamento.dto.FornecedorResponse;
import com.api.treinamento.entity.Fornecedor;
import com.api.treinamento.repository.FornecedorRepository;
import com.api.treinamento.service.FornecedorService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FornecedorServiceImpl implements FornecedorService {

    private final FornecedorRepository repository;

    @Override
    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        log.info("Criando fornecedor com CNPJ: {}", request.getCnpj());

        validarCnpjUnico(request.getCnpj(), null);

        Fornecedor fornecedor = toEntity(request);
        fornecedor = repository.save(fornecedor);

        log.info("Fornecedor criado com id: {}", fornecedor.getId());
        return toResponse(fornecedor);
    }
    
    
    @Override
    @Transactional
    public FornecedorResponse criarException(FornecedorRequest request) {
        log.info("Criando fornecedor com CNPJ: {}", request.getCnpj());

        validarCnpjUnico(request.getCnpj(), null);

        Fornecedor fornecedor = toEntity(request);
        fornecedor = repository.save(fornecedor);

        log.info("Fornecedor criado com id: {}", fornecedor.getId());
        return toResponse(fornecedor);
    }
   

    @Override
    @Transactional
    public FornecedorResponse atualizar(Long id, FornecedorRequest request) {
        log.info("Atualizando fornecedor id: {}", id);

        Fornecedor fornecedor = buscarEntidadePorId(id);

        validarCnpjUnico(request.getCnpj(), id);
        validarEmailUnico(request.getEmail(), id);

        fornecedor.setNome(request.getNome());
        fornecedor.setCnpj(request.getCnpj());
        fornecedor.setEmail(request.getEmail());
        fornecedor.setTelefone(request.getTelefone());
        fornecedor.setLogradouro(request.getLogradouro());
        fornecedor.setNumero(request.getNumero());
        fornecedor.setComplemento(request.getComplemento());
        fornecedor.setBairro(request.getBairro());
        fornecedor.setCidade(request.getCidade());
        fornecedor.setEstado(request.getEstado().toUpperCase());
        fornecedor.setCep(request.getCep());

        fornecedor = repository.save(fornecedor);

        log.info("Fornecedor id {} atualizado com sucesso", id);
        return toResponse(fornecedor);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando fornecedor id: {}", id);

        Fornecedor fornecedor = buscarEntidadePorId(id);
        fornecedor.setAtivo(false);
        repository.save(fornecedor);

        log.info("Fornecedor id {} inativado com sucesso", id);
    }

    @Override
    @Transactional(readOnly = true)
    public FornecedorResponse buscarPorId(Long id) {
        log.info("Buscando fornecedor id: {}", id);
        return toResponse(buscarEntidadePorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FornecedorResponse> buscarPorNome(String nome, Pageable pageable) {
        log.info("Buscando fornecedores pelo nome: {}", nome);

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O parâmetro 'nome' não pode ser vazio");
        }

        return repository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FornecedorResponse> listarTodos(Pageable pageable) {
        log.info("Listando todos os fornecedores ativos");
        return repository.findAllAtivos(pageable).map(this::toResponse);
    }

    // ─── Métodos privados ─────────────────────────────────────────────────────

    private Fornecedor buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com id: " + id));
    }

    private void validarCnpjUnico(String cnpj, Long idExcluido) {
        boolean existe = (idExcluido == null)
                ? repository.existsByCnpj(cnpj)
                : repository.existsByCnpjAndIdNot(cnpj, idExcluido);

        if (existe) {
            throw new IllegalArgumentException("Já existe um fornecedor cadastrado com o CNPJ: " + cnpj);
        }
    }

    private void validarEmailUnico(String email, Long idExcluido) {
        if (repository.existsByEmailAndIdNot(email, idExcluido)) {
            throw new IllegalArgumentException("Já existe um fornecedor cadastrado com o e-mail: " + email);
        }
    }

    private Fornecedor toEntity(FornecedorRequest request) {
        return Fornecedor.builder()
                .nome(request.getNome())
                .cnpj(request.getCnpj())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .logradouro(request.getLogradouro())
                .numero(request.getNumero())
                .complemento(request.getComplemento())
                .bairro(request.getBairro())
                .cidade(request.getCidade())
                .estado(request.getEstado().toUpperCase())
                .cep(request.getCep())
                .ativo(true)
                .build();
    }

    private FornecedorResponse toResponse(Fornecedor f) {
        String enderecoCompleto = String.format("%s, %s%s - %s, %s/%s - CEP: %s",
                f.getLogradouro(),
                f.getNumero(),
                (f.getComplemento() != null && !f.getComplemento().isBlank()) ? " " + f.getComplemento() : "",
                f.getBairro(),
                f.getCidade(),
                f.getEstado(),
                f.getCep());

        return FornecedorResponse.builder()
                .id(f.getId())
                .nome(f.getNome())
                .cnpj(f.getCnpj())
                .email(f.getEmail())
                .telefone(f.getTelefone())
                .logradouro(f.getLogradouro())
                .numero(f.getNumero())
                .complemento(f.getComplemento())
                .bairro(f.getBairro())
                .cidade(f.getCidade())
                .estado(f.getEstado())
                .cep(f.getCep())
                .enderecoCompleto(enderecoCompleto)
                .ativo(f.getAtivo())
                .criadoEm(f.getCriadoEm())
                .atualizadoEm(f.getAtualizadoEm())
                .build();
    }
}
