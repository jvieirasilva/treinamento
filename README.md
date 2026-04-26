# 🏢 Fornecedor API

API REST para gestão de fornecedores brasileiros, desenvolvida com **Spring Boot 3.4**, **Java 21**, **H2 Database** e documentada com **Swagger/OpenAPI**.

---

## 🚀 Como executar

### Pré-requisitos
- Java 21+
- Maven 3.9+

### Executar
```bash
./mvnw spring-boot:run
```
Ou:
```bash
mvn spring-boot:run
```

---

## 📚 Documentação

| Recurso        | URL                                        |
|----------------|--------------------------------------------|
| Swagger UI     | http://localhost:8080/swagger-ui.html      |
| API Docs (JSON)| http://localhost:8080/api-docs             |
| H2 Console     | http://localhost:8080/h2-console           |

> **H2 Console:** JDBC URL: `jdbc:h2:mem:fornecedordb` | User: `sa` | Password: *(vazio)*

---

## 📌 Endpoints

| Método | Endpoint                        | Descrição                        |
|--------|---------------------------------|----------------------------------|
| POST   | `/api/v1/fornecedores`          | Criar fornecedor                 |
| PUT    | `/api/v1/fornecedores/{id}`     | Atualizar fornecedor             |
| DELETE | `/api/v1/fornecedores/{id}`     | Deletar fornecedor (soft delete) |
| GET    | `/api/v1/fornecedores/{id}`     | Buscar por ID                    |
| GET    | `/api/v1/fornecedores/buscar`   | Buscar por nome (paginado)       |
| GET    | `/api/v1/fornecedores`          | Listar todos (paginado)          |

### Parâmetros de paginação
- `page` — número da página (padrão: 0)
- `size` — itens por página (padrão: 10)
- `sortBy` — campo de ordenação (padrão: `nome`)
- `sortDir` — direção: `asc` ou `desc` (padrão: `asc`)

### Exemplo de requisição POST
```json
{
  "nome": "Fornecedor Exemplo Ltda",
  "cnpj": "12.345.678/0001-99",
  "email": "contato@exemplo.com.br",
  "telefone": "(11) 99999-0000",
  "logradouro": "Av. Paulista",
  "numero": "1000",
  "complemento": "Sala 10",
  "bairro": "Bela Vista",
  "cidade": "São Paulo",
  "estado": "SP",
  "cep": "01310-100"
}
```

---

## 🛠️ Tecnologias

- Spring Boot 3.4.1
- Java 21
- H2 Database (in-memory)
- Spring Data JPA
- Lombok
- SpringDoc OpenAPI 2.7.0 (Swagger)
- Bean Validation

## 🌐 CORS
CORS liberado para `http://localhost:3000` (React).
"# treinamento" 
