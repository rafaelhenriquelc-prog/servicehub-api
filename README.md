# PetServiceHub API

API REST para cadastro e gestão de tutores, animais de estimação, serviços e agendamentos de um hub de atendimento pet.

O PetServiceHub centraliza o fluxo operacional do estabelecimento: o tutor é cadastrado, seus pets são vinculados a ele, os serviços oferecidos ficam disponíveis no catálogo e os agendamentos unem pet, tutor e serviço em uma data e hora específicas.

## Tecnologias

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Banco de dados H2 (em memória)
- Springdoc OpenAPI / Swagger UI
- Lombok
- Maven
- JUnit 5, MockMvc e Mockito

## Entidades e relacionamentos

| Entidade | Campos principais |
|---|---|
| **Tutor** | id, nome, e-mail, telefone, ativo |
| **Pet** | id, nome, espécie, raça, idade, tutor, ativo |
| **Serviço** | id, nome, descrição, preço, duração em minutos, ativo |
| **Agendamento** | id, pet, tutor, serviço, data e hora, observação, status |

Relacionamentos:

- Um tutor pode ter vários pets.
- Cada pet pertence a um único tutor.
- Cada agendamento referencia um pet, um tutor e um serviço.
- O pet informado no agendamento precisa pertencer ao tutor informado.
- O status do agendamento pode ser `AGENDADO`, `CONCLUIDO` ou `CANCELADO`.

## Principais endpoints

### Tutores — `/api/tutores`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/tutores` | Cadastra um tutor |
| `GET` | `/api/tutores` | Lista tutores (`?ativo=`) |
| `GET` | `/api/tutores/{id}` | Busca um tutor pelo id |
| `GET` | `/api/tutores/{id}/pets` | Lista os pets do tutor |
| `PUT` | `/api/tutores/{id}` | Atualiza um tutor |
| `DELETE` | `/api/tutores/{id}` | Exclui um tutor |

### Pets — `/api/pets`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/pets` | Cadastra um pet vinculado a um tutor |
| `GET` | `/api/pets` | Lista pets (`?ativo=` e `?tutorId=`) |
| `GET` | `/api/pets/{id}` | Busca um pet pelo id |
| `PUT` | `/api/pets/{id}` | Atualiza um pet |
| `DELETE` | `/api/pets/{id}` | Exclui um pet |

### Serviços — `/api/servicos`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/servicos` | Cadastra um serviço |
| `GET` | `/api/servicos` | Lista serviços (`?ativo=`) |
| `GET` | `/api/servicos/{id}` | Busca um serviço pelo id |
| `PUT` | `/api/servicos/{id}` | Atualiza um serviço |
| `DELETE` | `/api/servicos/{id}` | Exclui um serviço |

### Agendamentos — `/api/agendamentos`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/agendamentos` | Cadastra um agendamento |
| `GET` | `/api/agendamentos` | Lista agendamentos (`?status=`, `?tutorId=`, `?petId=`, `?servicoId=`) |
| `GET` | `/api/agendamentos/{id}` | Busca um agendamento pelo id |
| `PUT` | `/api/agendamentos/{id}` | Atualiza um agendamento |
| `DELETE` | `/api/agendamentos/{id}` | Exclui um agendamento |

## Códigos HTTP

| Código | Quando é utilizado |
|---|---|
| **200 OK** | Consulta ou atualização concluída com sucesso |
| **201 Created** | Recurso cadastrado com sucesso |
| **204 No Content** | Exclusão concluída com sucesso |
| **400 Bad Request** | Dados inválidos, tutor/pet/serviço inativo ou pet que não pertence ao tutor |
| **404 Not Found** | Recurso não encontrado |
| **409 Conflict** | E-mail de tutor duplicado ou exclusão bloqueada por vínculos |
| **500 Internal Server Error** | Erro inesperado no processamento da requisição |

## Como executar os testes

É necessário ter o JDK 21 instalado.

Na raiz do projeto:

```powershell
.\mvnw.cmd test
```

O comando esperado ao final é `BUILD SUCCESS`.

## Como iniciar a aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponível em `http://localhost:8080`.

## Swagger

Documentação interativa da API:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Especificação OpenAPI em JSON:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Console H2

O banco em memória pode ser inspecionado em:

[http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Dados de conexão:

- **JDBC URL:** `jdbc:h2:mem:petservicehub`
- **User Name:** `sa`
- **Password:** (deixar em branco)

## Ordem de cadastro

Para montar um fluxo completo, cadastre os recursos nesta ordem:

1. **Tutor** em `POST /api/tutores`
2. **Pet** em `POST /api/pets`, informando o `tutorId` retornado no passo anterior
3. **Serviço** em `POST /api/servicos`
4. **Agendamento** em `POST /api/agendamentos`, informando `petId`, `tutorId` e `servicoId`

Exemplo resumido:

```json
POST /api/tutores
{
  "nome": "Ana Souza",
  "email": "ana.souza@email.com",
  "telefone": "11988887777"
}

POST /api/pets
{
  "nome": "Thor",
  "especie": "Cachorro",
  "raca": "Labrador",
  "idade": 4,
  "tutorId": 1
}

POST /api/servicos
{
  "nome": "Banho e tosa",
  "descricao": "Banho completo com tosa higiênica",
  "preco": 89.90,
  "duracaoMinutos": 60
}

POST /api/agendamentos
{
  "petId": 1,
  "tutorId": 1,
  "servicoId": 1,
  "dataHora": "2026-12-15T14:30:00",
  "observacao": "Pet fica nervoso com secador",
  "status": "AGENDADO"
}
```
