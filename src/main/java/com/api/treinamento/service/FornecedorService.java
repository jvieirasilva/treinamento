package com.api.treinamento.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.api.treinamento.dto.FornecedorRequest;
import com.api.treinamento.dto.FornecedorResponse;

public interface FornecedorService {

    FornecedorResponse criar(FornecedorRequest request);
    
    public FornecedorResponse criarException(FornecedorRequest request);

    FornecedorResponse atualizar(Long id, FornecedorRequest request);

    void deletar(Long id);

    FornecedorResponse buscarPorId(Long id);

    Page<FornecedorResponse> buscarPorNome(String nome, Pageable pageable);

    Page<FornecedorResponse> listarTodos(Pageable pageable);
}
