# MicroBio Backend - Autenticação JWT REST API

## ✅ Implementação Finalizada

A autenticação foi implementada com sucesso utilizando:
- **JWT (JSON Web Tokens)** para autenticação stateless
- **BCrypt** para hash de senhas
- **Spring Security 6** configurado para API REST
- **H2 Database** para dados de teste
- **Sem Thymeleaf** - apenas REST API

---

## 📋 Arquivos Criados e Modificados

### ✨ Arquivos Criados
1. **`src/main/java/.../security/JwtService.java`**
   - Geração, validação e extração de tokens JWT
   - Métodos: generateToken(), validateToken(), getUsernameFromToken(), getRolesFromToken()

2. **`src/main/java/.../security/JwtAuthenticationFilter.java`**
   - Filtro que intercepta requisições e valida tokens
   - Lê header Authorization: Bearer TOKEN
   - Autentica automaticamente no SecurityContext

### 🔧 Arquivos Modificados
1. **`pom.xml`**
   - Adicionado: JJWT (jjwt-api, jjwt-impl, jjwt-jackson) v0.12.3

2. **`src/main/resources/application.properties`**
   - Adicionada configuração de banco de dados (H2)
   - Adicionado JWT secret e expiration

3. **`src/main/java/.../dto/AuthResponse.java`**
   - Adicionado campo `token` e `role`
   - Construtor sobrecargado para compatibilidade

4. **`src/main/java/.../controller/AuthController.java`**
   - Integração com JwtService
   - Endpoints retornam token em sucesso

5. **`src/main/java/.../config/SecurityConfig.java`**
   - Integração com JwtAuthenticationFilter
   - SessionCreationPolicy.STATELESS
   - Removido formLogin()

6. **`src/main/java/.../controller/LoginController.java`**
   - Marcado como @Deprecated - não renderiza mais página

---

## 🔑 Credenciais de Teste

Usuários inicializados automaticamente na primeira execução:

```
Admin:
  Username: admin
  Password: admin123
  Role: ADMIN

Usuário Comum:
  Username: user
  Password: user123
  Role: USER
```

---

## 🔌 Endpoints REST

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Resposta (201) - Sucesso:**
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "username": "admin",
  "token": "eyJhbGc...",
  "role": "ADMIN"
}
```

**Resposta (401) - Erro:**
```json
{
  "success": false,
  "message": "Usuário ou senha incorretos",
  "username": null,
  "token": null,
  "role": null
}
```

### Logout
```http
POST /api/auth/logout
```

**Resposta:**
```json
{
  "success": true,
  "message": "Logout realizado com sucesso",
  "username": null,
  "token": null,
  "role": null
}
```

### Obter Usuário Autenticado
```http
GET /api/auth/me
Authorization: Bearer {token}
```

**Resposta (200) - Autenticado:**
```json
{
  "success": true,
  "message": "Usuário autenticado",
  "username": "admin",
  "token": null,
  "role": "ADMIN"
}
```

**Resposta (401) - Não autenticado:**
```json
{
  "success": false,
  "message": "Não autenticado",
  "username": null,
  "token": null,
  "role": null
}
```

---

## 🔐 Fluxo de Autenticação

```
1. Cliente (React) faz POST /api/auth/login com email/username e password
   ↓
2. AuthController recebe requisição
   ↓
3. AuthenticationManager autentica com DaoAuthenticationProvider
   ↓
4. CustomUserDetailsService busca usuário no banco pelo username
   ↓
5. BCryptPasswordEncoder valida a senha
   ↓
6. Se sucesso, JwtService gera token JWT
   ↓
7. Token é retornado no response (AuthResponse)
   ↓
8. Cliente armazena token (localStorage)
   ↓
9. Cliente envia token em requisições: Authorization: Bearer {token}
   ↓
10. JwtAuthenticationFilter intercepta requisição
    ↓
11. Extrai token do header
    ↓
12. JwtService valida token
    ↓
13. Se válido, autentica usuário no SecurityContext
    ↓
14. Requisição é processada com usuário autenticado
```

---

## 🚀 Iniciar a Aplicação

### Opção 1: Maven Wrapper
```bash
cd MicroBio-Backend
./mvnw spring-boot:run
```

### Opção 2: Build e JAR
```bash
cd MicroBio-Backend
./mvnw clean package
java -jar target/application-0.0.1-SNAPSHOT.jar
```

A aplicação será iniciada em: **http://localhost:8080**

---

## 📝 Exemplos de Requisições

### 1️⃣ Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Resposta:
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "username": "admin",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "role": "ADMIN"
}
```

### 2️⃣ Chamar Endpoint Protegido
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# Exemplo: Obter informações do usuário
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### 3️⃣ React Fetch Example
```javascript
// Login
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
});

const { token, role, username } = await loginResponse.json();

// Armazenar token
localStorage.setItem('token', token);

// Chamar API protegida
const apiResponse = await fetch('http://localhost:8080/api/servicos', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await apiResponse.json();
```

---

## ⚙️ Configuração

### `application.properties`
```properties
# JWT
jwt.secret=sua-chave-secreta-bem-comprida-para-ambiente-de-desenvolvimento-2024
jwt.expiration=86400000  # 24 horas em milissegundos

# Database (H2 em memória para desenvolvimento)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### CORS
Configurado em `SecurityConfig.java`:
- `http://localhost:3000` ✓
- `http://localhost:5173` ✓

---

## ✨ Estrutura de Diretórios

```
src/main/java/com/microbio/application/
├── security/                      ← NOVO
│   ├── JwtService.java           ← NOVO
│   └── JwtAuthenticationFilter.java ← NOVO
├── config/
│   ├── SecurityConfig.java        ✏️ MODIFICADO
│   ├── DataInitializer.java       ✓ PRONTO
│   └── CorsConfig.java
├── controller/
│   ├── AuthController.java        ✏️ MODIFICADO
│   └── LoginController.java       (Deprecated)
├── service/
│   └── CustomUserDetailsService.java ✓ PRONTO
├── model/
│   └── Usuario.java               ✓ PRONTO
├── repository/
│   └── UsuarioRepository.java     ✓ PRONTO
├── dto/
│   ├── LoginRequest.java          ✓ PRONTO
│   └── AuthResponse.java          ✏️ MODIFICADO
└── ...
```

---

## 🔍 Troubleshooting

### ❌ "Senha gerada automaticamente"
**Problema:** Spring Security gera senha aleatória na primeira execução
**Solução:** Já foi removido - usamos BCrypt com senhas definidas em `DataInitializer`

### ❌ Token inválido
**Verificar:**
1. Header: `Authorization: Bearer {token}` (espaço após Bearer)
2. Secret nos dois lados não combina
3. Token expirado (24 horas)

### ❌ CORS Error
**Solução:** Verificar que React está em `localhost:5173` ou `localhost:3000`

### ❌ 401 Unauthorized
1. Token inválido ou ausente
2. Usuário não autenticado
3. Header Authorization não enviado corretamente

---

## 🎯 Próximas Etapas (Frontend React)

Frontend deve:
1. ✅ POST /api/auth/login com credenciais
2. ✅ Armazenar token (localStorage)
3. ✅ Enviar token em requisições: `Authorization: Bearer {token}`
4. ✅ Tratarresposta 401 (token expirado → novo login)
5. ✅ Implementar Logout (limpar localStorage)

---

## 📚 Documentação

- [Spring Security 6](https://spring.io/projects/spring-security)
- [JJWT](https://github.com/jwtk/jjwt)
- [JWT Standard](https://tools.ietf.org/html/rfc7519)
- [BCrypt](https://en.wikipedia.org/wiki/Bcrypt)

---

**Última atualização:** 2026-04-29
**Status:** ✅ FINALIZADO E TESTADO

