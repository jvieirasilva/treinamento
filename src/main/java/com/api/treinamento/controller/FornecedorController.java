package com.api.treinamento.controller;

import com.api.treinamento.dto.ApiResponse;
import com.api.treinamento.dto.FornecedorRequest;
import com.api.treinamento.dto.FornecedorResponse;
import com.api.treinamento.repository.FornecedorRepository;
import com.api.treinamento.service.FornecedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores", description = "API de Gestão de Fornecedores")
public class FornecedorController {

    private final FornecedorService service;


    @PostMapping
    @Operation(summary = "Criar fornecedor", description = "Cadastra um novo fornecedor")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<ApiResponse<FornecedorResponse>> criar(
            @Valid @RequestBody FornecedorRequest request) {
    	
    	
        FornecedorResponse response = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Fornecedor criado com sucesso", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor", description = "Atualiza os dados de um fornecedor existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fornecedor atualizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    public ResponseEntity<ApiResponse<FornecedorResponse>> atualizar(
            @Parameter(description = "ID do fornecedor") @PathVariable Long id,
            @Valid @RequestBody FornecedorRequest request) {
        FornecedorResponse response = service.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.sucesso("Fornecedor atualizado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar fornecedor", description = "Inativa um fornecedor (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fornecedor removido com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> deletar(
            @Parameter(description = "ID do fornecedor") @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.ok(ApiResponse.sucesso("Fornecedor removido com sucesso", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna um fornecedor pelo seu ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    public ResponseEntity<ApiResponse<FornecedorResponse>> buscarPorId(
            @Parameter(description = "ID do fornecedor") @PathVariable Long id
            ) {
        FornecedorResponse response = service.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.sucesso("Fornecedor encontrado", response));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nome", description = "Pesquisa fornecedores pelo nome com paginação")
    public ResponseEntity<ApiResponse<Page<FornecedorResponse>>> buscarPorNome(
            @Parameter(description = "Nome ou parte do nome do fornecedor")
            @RequestParam String nome,
            @Parameter(description = "Número da página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "nome") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FornecedorResponse> resultado = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(ApiResponse.sucesso("Busca realizada com sucesso", resultado));
    }

    @GetMapping
    @Operation(summary = "Listar todos", description = "Lista todos os fornecedores ativos com paginação")
    public ResponseEntity<ApiResponse<Page<FornecedorResponse>>> listarTodos(
            @Parameter(description = "Número da página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "nome") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FornecedorResponse> resultado = service.listarTodos(pageable);
        return ResponseEntity.ok(ApiResponse.sucesso("Fornecedores listados com sucesso", resultado));
    }
}
