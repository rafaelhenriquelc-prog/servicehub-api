# ServiceHub API

API REST da plataforma ServiceHub. Clientes localizam prestadores, contratam serviços e registram avaliações após a conclusão do atendimento.

O projeto Maven continua se chamando `petservicehub-api`, mas o domínio da aplicação é o ServiceHub da disciplina.

## Tecnologias

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- PostgreSQL (execução da aplicação)
- H2 em memória (apenas nos testes)
- Springdoc OpenAPI / Swagger UI
- Lombok
- Maven
- JUnit 5, MockMvc e Mockito

A senha do usuário é recebida no cadastro e gravada somente como hash (`passwordHash`). A API nunca devolve a senha. Não há autenticação nem JWT nesta versão.

## Entidades e relacionamentos

| Entidade | Campos principais |
|---|---|
| **User** | id, fullName, email (único), passwordHash, phone, bio, avatarUrl, role (`CLIENT` ou `PROVIDER`), active, createdAt, updatedAt |
| **Service** | id, title, description, price, category, provider (User), active, createdAt |
| **ServiceRequest** | id, service, client (User), status, scheduledAt, notes, totalPrice, createdAt, updatedAt |
| **Review** | id, request (relação única), reviewer (User), rating (1 a 5), comment, createdAt |

Relacionamentos e regras:

- Somente um usuário `PROVIDER` pode ser associado como prestador de um serviço.
- Somente um usuário `CLIENT` pode contratar um serviço.
- O `totalPrice` da solicitação é preenchido com o preço do serviço no momento do cadastro ou da atualização.
- Status da solicitação: `PENDING`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED` ou `CANCELLED`.
- A avaliação só pode ser cadastrada para uma solicitação `COMPLETED`.
- O avaliador deve ser o cliente da solicitação.
- Uma solicitação pode receber somente uma avaliação.

## Principais endpoints

### Usuários — `/api/users`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/users` | Cadastra um cliente ou prestador |
| `GET` | `/api/users` | Lista usuários (`?role=` e `?active=`) |
| `GET` | `/api/users/{id}` | Busca um usuário pelo id |
| `PUT` | `/api/users/{id}` | Atualiza um usuário |
| `DELETE` | `/api/users/{id}` | Exclui um usuário |

### Serviços — `/api/services`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/services` | Cadastra um serviço de um prestador |
| `GET` | `/api/services` | Lista serviços (`?active=`, `?providerId=` e `?category=`) |
| `GET` | `/api/services/{id}` | Busca um serviço pelo id |
| `PUT` | `/api/services/{id}` | Atualiza um serviço |
| `DELETE` | `/api/services/{id}` | Exclui um serviço |

### Solicitações — `/api/service-requests`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/service-requests` | Cadastra uma contratação |
| `GET` | `/api/service-requests` | Lista solicitações (`?status=`, `?clientId=` e `?serviceId=`) |
| `GET` | `/api/service-requests/{id}` | Busca uma solicitação pelo id |
| `PUT` | `/api/service-requests/{id}` | Atualiza uma solicitação |
| `DELETE` | `/api/service-requests/{id}` | Exclui uma solicitação |

### Avaliações — `/api/reviews`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/reviews` | Cadastra uma avaliação |
| `GET` | `/api/reviews` | Lista avaliações (`?reviewerId=` e `?requestId=`) |
| `GET` | `/api/reviews/{id}` | Busca uma avaliação pelo id |
| `PUT` | `/api/reviews/{id}` | Atualiza uma avaliação |
| `DELETE` | `/api/reviews/{id}` | Exclui uma avaliação |

## Códigos HTTP

| Código | Quando é utilizado |
|---|---|
| **200 OK** | Consulta ou atualização concluída com sucesso |
| **201 Created** | Recurso cadastrado com sucesso |
| **204 No Content** | Exclusão concluída com sucesso |
| **400 Bad Request** | Dados inválidos ou regra de negócio (perfil incompatível, serviço inativo, avaliação fora das regras) |
| **404 Not Found** | Recurso não encontrado |
| **409 Conflict** | E-mail duplicado, avaliação duplicada ou exclusão bloqueada por vínculos |
| **500 Internal Server Error** | Erro inesperado no processamento da requisição |

## Como executar os testes

É necessário ter o JDK 21 instalado. Os testes usam H2 em memória e **não** precisam do PostgreSQL.

Na raiz do projeto:

```powershell
.\mvnw.cmd test
```

O comando esperado ao final é `BUILD SUCCESS`.

## Como iniciar a aplicação (PostgreSQL)

A execução local usa PostgreSQL. URL, usuário e senha são lidos das variáveis de ambiente `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD`. Não grave senha no código nem no Git.

### 1. Instalar o PostgreSQL (Windows)

Instale o PostgreSQL e, durante a instalação, anote a senha do usuário `postgres`. Essa senha não deve ser commitada.

### 2. Criar o banco

No SQL Shell (`psql`) ou no pgAdmin, execute:

```sql
CREATE DATABASE petservicehub;
```

### 3. Configurar as variáveis de ambiente (PowerShell)

Na mesma janela em que a aplicação será iniciada:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/petservicehub"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="coloque-aqui-a-senha-do-postgres"
```

Substitua `coloque-aqui-a-senha-do-postgres` pela senha definida na instalação. Se o banco, o usuário ou a porta forem diferentes, ajuste `DATABASE_URL` e `DATABASE_USERNAME`.

Para persistir as variáveis na sessão do Windows (usuário atual):

```powershell
setx DATABASE_URL "jdbc:postgresql://localhost:5432/petservicehub"
setx DATABASE_USERNAME "postgres"
setx DATABASE_PASSWORD "coloque-aqui-a-senha-do-postgres"
```

Depois do `setx`, feche e abra o terminal para as variáveis valerem.

### 4. Iniciar a API

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponível em `http://localhost:8080`. As tabelas são criadas ou atualizadas automaticamente (`spring.jpa.hibernate.ddl-auto=update`).

## Swagger

Documentação interativa da API:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Especificação OpenAPI em JSON:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Ordem de cadastro

Para montar um fluxo completo, cadastre os recursos nesta ordem:

1. **Prestador** em `POST /api/users` com `"role": "PROVIDER"`
2. **Cliente** em `POST /api/users` com `"role": "CLIENT"`
3. **Serviço** em `POST /api/services`, informando o `providerId` do prestador
4. **Solicitação** em `POST /api/service-requests`, informando `serviceId` e `clientId`
5. Conclua a solicitação com `PUT /api/service-requests/{id}` e `"status": "COMPLETED"`
6. **Avaliação** em `POST /api/reviews`, informando `requestId` e o `reviewerId` do cliente

Exemplo resumido:

```json
POST /api/users
{
  "fullName": "Carlos Lima",
  "email": "carlos.lima@email.com",
  "password": "senha123",
  "phone": "11977776666",
  "role": "PROVIDER"
}

POST /api/users
{
  "fullName": "Ana Souza",
  "email": "ana.souza@email.com",
  "password": "senha123",
  "phone": "11988887777",
  "role": "CLIENT"
}

POST /api/services
{
  "title": "Montagem de móveis",
  "description": "Montagem de móveis residenciais e corporativos",
  "price": 150.00,
  "category": "Marcenaria",
  "providerId": 1
}

POST /api/service-requests
{
  "serviceId": 1,
  "clientId": 2,
  "scheduledAt": "2026-09-20T14:30:00",
  "notes": "Apartamento no 3º andar, sem elevador"
}

PUT /api/service-requests/1
{
  "serviceId": 1,
  "clientId": 2,
  "status": "COMPLETED",
  "scheduledAt": "2026-09-20T14:30:00"
}

POST /api/reviews
{
  "requestId": 1,
  "reviewerId": 2,
  "rating": 5,
  "comment": "Serviço pontual e muito bem feito"
}
```
