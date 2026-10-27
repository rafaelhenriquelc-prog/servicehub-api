# ServiceHub API

API REST da plataforma ServiceHub. Clientes localizam prestadores, contratam serviços e registram avaliações após a conclusão do atendimento.

O projeto Maven continua se chamando `petservicehub-api`, mas o domínio da aplicação é o ServiceHub da disciplina.

## Tecnologias

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA
- Spring Security (JWT stateless, BCrypt e `@EnableMethodSecurity`)
- Bean Validation
- PostgreSQL (execução da aplicação)
- H2 em memória (apenas nos testes)
- Springdoc OpenAPI / Swagger UI
- Lombok
- Maven
- JUnit 5, MockMvc, Mockito e spring-security-test

A senha é recebida no cadastro e gravada somente como hash BCrypt (`passwordHash`). A API nunca devolve senha nem hash. A autenticação usa JWT no header `Authorization: Bearer <token>`.

## Autenticação e autorização

Rotas públicas:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/services` e `GET /api/services/{id}`
- `/swagger-ui/**`, `/swagger-ui.html` e `/v3/api-docs/**`

Demais rotas exigem JWT. Token inválido, expirado ou de usuário inativo retorna **401**. Papel insuficiente retorna **403**.

Papéis (`UserRole`): `ADMIN`, `PROVIDER` e `CLIENT`.

| Papel | Permissões principais |
|---|---|
| **ADMIN** | Administra usuários (criar, listar, atualizar, excluir) |
| **PROVIDER** | Cria serviços e altera/exclui somente os próprios |
| **CLIENT** | Cria solicitações, acessa somente as próprias e avalia somente solicitação própria `COMPLETED` |

O prestador também consulta solicitações e avaliações dos próprios serviços. IDs de dono (`providerId`, `clientId`, `reviewerId`) **não** são aceitos no corpo das operações de escrita: o usuário autenticado define o proprietário.

O cadastro público (`POST /api/auth/register`) aceita somente `CLIENT` ou `PROVIDER`. Tentativa de cadastrar `ADMIN` retorna **400**. O e-mail é gravado em minúsculas.

## Variáveis de ambiente

Copie `.env.example` para `.env` (o arquivo `.env` não deve ser commitado) ou defina as variáveis no terminal.

| Variável | Uso |
|---|---|
| `DATABASE_URL` | JDBC do PostgreSQL |
| `DATABASE_USERNAME` | Usuário do banco |
| `DATABASE_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Segredo HMAC do JWT (mínimo 32 caracteres). **Não use o valor de exemplo em produção.** |
| `JWT_EXPIRATION_MS` | Validade do token em milissegundos (padrão `3600000`) |

## Entidades e relacionamentos

| Entidade | Campos principais |
|---|---|
| **User** | id, fullName, email (único), passwordHash, phone, bio, avatarUrl, role (`ADMIN`, `CLIENT` ou `PROVIDER`), active, createdAt, updatedAt |
| **Service** | id, title, description, price, category, provider (User), active, createdAt |
| **ServiceRequest** | id, service, client (User), status, scheduledAt, notes, totalPrice, createdAt, updatedAt |
| **Review** | id, request (relação única), reviewer (User), rating (1 a 5), comment, createdAt |

Relacionamentos e regras:

- Somente um usuário `PROVIDER` pode publicar um serviço.
- Somente um usuário `CLIENT` pode contratar um serviço.
- O `totalPrice` da solicitação é preenchido com o preço do serviço no momento do cadastro.
- Status da solicitação: `PENDING`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED` ou `CANCELLED`.
- A avaliação só pode ser cadastrada para uma solicitação `COMPLETED` pelo próprio cliente.
- Uma solicitação pode receber somente uma avaliação.

## Principais endpoints

### Autenticação — `/api/auth`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cadastro público de `CLIENT` ou `PROVIDER` |
| `POST` | `/api/auth/login` | Login com e-mail e senha; devolve JWT e dados públicos |

### Usuários — `/api/users`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/users` | ADMIN cadastra um usuário |
| `GET` | `/api/users` | ADMIN lista usuários (`?role=` e `?active=`) |
| `GET` | `/api/users/{id}` | ADMIN ou o próprio usuário |
| `PUT` | `/api/users/{id}` | ADMIN ou o próprio usuário |
| `DELETE` | `/api/users/{id}` | ADMIN exclui um usuário |

### Serviços — `/api/services`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/services` | PROVIDER cadastra um serviço (dono = token) |
| `GET` | `/api/services` | Lista pública (`?active=`, `?providerId=` e `?category=`) |
| `GET` | `/api/services/{id}` | Consulta pública |
| `PUT` | `/api/services/{id}` | PROVIDER altera o próprio serviço; ADMIN altera qualquer um |
| `DELETE` | `/api/services/{id}` | PROVIDER exclui o próprio serviço; ADMIN exclui qualquer um |

### Solicitações — `/api/service-requests`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/service-requests` | CLIENT contrata um serviço (cliente = token) |
| `GET` | `/api/service-requests` | Lista no escopo do autenticado (`?status=`, `?clientId=` e `?serviceId=`) |
| `GET` | `/api/service-requests/{id}` | CLIENT/PROVIDER no próprio escopo; ADMIN qualquer uma |
| `PUT` | `/api/service-requests/{id}` | CLIENT altera a própria; PROVIDER atualiza status das suas |
| `DELETE` | `/api/service-requests/{id}` | Exclusão no próprio escopo |

### Avaliações — `/api/reviews`

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/reviews` | CLIENT avalia solicitação própria concluída (avaliador = token) |
| `GET` | `/api/reviews` | Lista no escopo do autenticado (`?reviewerId=` e `?requestId=`) |
| `GET` | `/api/reviews/{id}` | Acesso no próprio escopo |
| `PUT` | `/api/reviews/{id}` | CLIENT atualiza a própria |
| `DELETE` | `/api/reviews/{id}` | CLIENT exclui a própria |

## Códigos HTTP

| Código | Quando é utilizado |
|---|---|
| **200 OK** | Consulta ou atualização concluída com sucesso |
| **201 Created** | Recurso cadastrado com sucesso |
| **204 No Content** | Exclusão concluída com sucesso |
| **400 Bad Request** | Dados inválidos, cadastro de ADMIN no registro público ou regra de negócio |
| **401 Unauthorized** | Sem token, token inválido/expirado, usuário inativo ou credenciais inválidas |
| **403 Forbidden** | Autenticado, mas sem permissão (papel ou recurso de outro usuário) |
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

A execução local usa PostgreSQL. URL, usuário e senha são lidos das variáveis de ambiente `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD`. O JWT usa `JWT_SECRET` e `JWT_EXPIRATION_MS`. Não grave senha nem segredo JWT no código nem no Git.

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
$env:JWT_SECRET="altere-este-segredo-jwt-com-pelo-menos-32-chars"
$env:JWT_EXPIRATION_MS="3600000"
```

Substitua os valores de exemplo. Se o banco, o usuário ou a porta forem diferentes, ajuste `DATABASE_URL` e `DATABASE_USERNAME`.

Para persistir as variáveis na sessão do Windows (usuário atual):

```powershell
setx DATABASE_URL "jdbc:postgresql://localhost:5432/petservicehub"
setx DATABASE_USERNAME "postgres"
setx DATABASE_PASSWORD "coloque-aqui-a-senha-do-postgres"
setx JWT_SECRET "altere-este-segredo-jwt-com-pelo-menos-32-chars"
setx JWT_EXPIRATION_MS "3600000"
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

No Swagger, use o esquema **bearer-jwt**: faça login em `POST /api/auth/login`, copie o `token` e informe-o em **Authorize**.

## Ordem de cadastro

Para montar um fluxo completo:

1. **Prestador** em `POST /api/auth/register` com `"role": "PROVIDER"`
2. **Cliente** em `POST /api/auth/register` com `"role": "CLIENT"`
3. Faça login em `POST /api/auth/login` e envie o JWT nas rotas autenticadas
4. **Serviço** em `POST /api/services` com o token do prestador (sem `providerId` no corpo)
5. **Solicitação** em `POST /api/service-requests` com o token do cliente (sem `clientId` no corpo)
6. Conclua a solicitação com `PUT /api/service-requests/{id}` e `"status": "COMPLETED"` usando o token do prestador
7. **Avaliação** em `POST /api/reviews` com o token do cliente (sem `reviewerId` no corpo)

Exemplo resumido:

```json
POST /api/auth/register
{
  "fullName": "Carlos Lima",
  "email": "carlos.lima@email.com",
  "password": "senha123",
  "phone": "11977776666",
  "role": "PROVIDER"
}

POST /api/auth/register
{
  "fullName": "Ana Souza",
  "email": "ana.souza@email.com",
  "password": "senha123",
  "phone": "11988887777",
  "role": "CLIENT"
}

POST /api/auth/login
{
  "email": "carlos.lima@email.com",
  "password": "senha123"
}

POST /api/services
Authorization: Bearer <token-do-prestador>
{
  "title": "Montagem de móveis",
  "description": "Montagem de móveis residenciais e corporativos",
  "price": 150.00,
  "category": "Marcenaria"
}

POST /api/service-requests
Authorization: Bearer <token-do-cliente>
{
  "serviceId": 1,
  "scheduledAt": "2026-09-20T14:30:00",
  "notes": "Apartamento no 3º andar, sem elevador"
}

PUT /api/service-requests/1
Authorization: Bearer <token-do-prestador>
{
  "serviceId": 1,
  "status": "COMPLETED"
}

POST /api/reviews
Authorization: Bearer <token-do-cliente>
{
  "requestId": 1,
  "rating": 5,
  "comment": "Serviço pontual e muito bem feito"
}
```
