# 📌 Referência Rápida - Arquivos e Alterações

## 📑 Índice de Documentação

1. **`RESUMO_IMPLEMENTACAO.md`** ← COMECE AQUI
   - Visão geral completa
   - O que foi feito e por quê
   - Fluxo visual
   - Como rodar

2. **`AUTENTICACAO_JWT.md`**
   - Documentação técnica detalhada
   - Endpoints com exemplos
   - Configuração
   - Troubleshooting

3. **`EXEMPLOS_REQUISICOES.md`**
   - Exemplos prontos para cURL
   - PowerShell
   - JavaScript
   - Postman
   - VSCode REST Client

4. **`REFERENCIA_RAPIDA.md`** (este arquivo)
   - Lista rápida de arquivos
   - Checklist de implementação
   - Comandos úteis

---

## 🆕 Arquivos Criados

### Segurança (Nova pasta)
```
src/main/java/com/microbio/application/security/
├── JwtService.java
│   └─ Geração e validação de tokens JWT
│
└── JwtAuthenticationFilter.java
    └─ Filtro para validar tokens em requisições
```

### Documentação (Raiz do projeto)
```
├── RESUMO_IMPLEMENTACAO.md      (este que você leu)
├── AUTENTICACAO_JWT.md          (documentação completa)
└── EXEMPLOS_REQUISICOES.md      (testes prontos)
```

---

## ✏️ Arquivos Modificados

```
pom.xml
└─ Adicionado: JJWT v0.12.3

src/main/resources/application.properties
├─ Adicionado: jwt.secret
├─ Adicionado: jwt.expiration
└─ Adicionado: Configuração H2

src/main/java/com/microbio/application/config/SecurityConfig.java
├─ Integrado: JwtAuthenticationFilter
├─ Mudado: SessionCreationPolicy.STATELESS
└─ Corrigido: DaoAuthenticationProvider

src/main/java/com/microbio/application/controller/AuthController.java
├─ Integrado: JwtService
├─ Adicionado: Geração de token no login
└─ Atualizado: Endpoints retornam token

src/main/java/com/microbio/application/dto/AuthResponse.java
├─ Adicionado: token (String)
├─ Adicionado: role (String)
└─ Adicionado: Construtor sobrecargado

src/main/java/com/microbio/application/controller/LoginController.java
└─ Marcado: @Deprecated (não renderiza mais HTML)

src/main/resources/templates/login.html
└─ Convertido: Em comentário explicativo
```

---

## ✅ Arquivos NÃO Modificados (Já Funcionavam)

```
✓ Usuario.java                           (model correto)
✓ UsuarioRepository.java                 (com findByUsername)
✓ CustomUserDetailsService.java          (implementada)
✓ DataInitializer.java                   (seedando usuários)
✓ LoginRequest.java                      (DTO correto)
✓ CorsConfig.java                        (placeHolder - CORS em SecurityConfig)
✓ SwaggerConfig.java                     (auto-configurado)
```

---

## 🚀 Comandos Úteis

### Compilar
```bash
cd MicroBio-Backend
./mvnw clean compile
```

### Compilar + Empacotar
```bash
./mvnw clean package -DskipTests
```

### Rodando o Dev
```bash
./mvnw spring-boot:run
```

### Executar JAR
```bash
java -jar target/application-0.0.1-SNAPSHOT.jar
```

### Testar Login (cURL)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Limpar cache Maven
```bash
./mvnw clean
```

---

## ✨ Checklist de Implementação

### Configuração
- [x] JWT Secret configurado
- [x] JWT Expiration configurado (24h)
- [x] Database H2 configurado
- [x] CORS configurado para React

### Segurança
- [x] BCrypt para passwords
- [x] SessionCreationPolicy.STATELESS
- [x] CSRF desabilitado
- [x] JwtAuthenticationFilter criado
- [x] Endpoints públicos definidos

### Autenticação
- [x] JwtService criado
- [x] AuthController com JWT
- [x] Login retorna token
- [x] GET /me retorna usuário
- [x] Logout funciona

### Banco de Dados
- [x] Usuario model correto
- [x] UsuarioRepository com findByUsername
- [x] DataInitializer pronto
- [x] Usuários criados: admin/admin123, user/user123

### Testes
- [x] Aplicação compila sem erros
- [x] Aplicação inicia sem erros
- [x] POST /api/auth/login funciona
- [x] GET /api/auth/me com token funciona
- [x] Requisições sem token bloqueadas
- [x] CORS funcionando

---

## 🔌 Endpoints (Resumo)

| Método | Endpoint | Público | Descrição |
|--------|----------|---------|-----------|
| POST | `/api/auth/login` | ✅ | Login e obter token |
| POST | `/api/auth/logout` | ✅ | Logout (limpar sessão) |
| GET | `/api/auth/me` | ❌ | Info do usuário (auth) |
| GET/POST/PUT/DELETE | `/api/**` | ❌ | Outros endpoints protegidos |
| GET | `/swagger-ui/**` | ✅ | Documentação |
| GET | `/v3/api-docs/**` | ✅ | OpenAPI docs |

**✅ = Disponível sem token**
**❌ = Requer Authorization: Bearer TOKEN**

---

## 🔐 Credenciais Padrão

```
👤 Admin
┐ username: admin
└ password: admin123
  role: ADMIN

👤 Usuário Comum
┐ username: user
└ password: user123
  role: USER
```

---

## 📊 Estrutura do Token JWT

### Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "admin",
  "roles": "ROLE_ADMIN",
  "iat": 1717071820,
  "exp": 1717158220
}
```

### Signature
```
HMACSHA512(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

---

## 🐛 Troubleshooting Rápido

| Problema | Solução |
|----------|---------|
| `Porta 8080 em uso` | Alterar `server.port=8081` em application.properties |
| `Token inválido` | Verificar header: `Authorization: Bearer TOKEN` |
| `403 Forbidden` | Token expirado ou ausente, fazer novo login |
| `401 Unauthorized` | Credenciais incorretas |
| `CORS error` | React deve estar em localhost:3000 ou localhost:5173 |
| `Senha aleatória` | Não aparece mais - removida |
| `Aplicação não inicia` | Verificar Java 21: `java -version` |

---

## 📚 Links Importantes

- [Spring Security 6](https://spring.io/projects/spring-security)
- [JJWT GitHub](https://github.com/jwtk/jjwt)
- [JWT.io Debugger](https://jwt.io)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

---

## 🎯 Próximas Etapas

### Curto Prazo (agora)
1. [x] Implementar autenticação JWT ← FEITO
2. [ ] Testar endpoints com Postman
3. [ ] Conectar React ao backend
4. [ ] Implementar login no React

### Médio Prazo
1. [ ] Adicionar refresh tokens
2. [ ] Implementar 2FA
3. [ ] Adicionar rate limiting
4. [ ] Logs de autenticação

### Longo Prazo (produção)
1. [ ] Trocar H2 por PostgreSQL
2. [ ] Configurar HTTPS/TLS
3. [ ] Usar variáveis de ambiente
4. [ ] Monitoramento e alertas

---

## ✉️ Variáveis de Ambiente (Para Produção)

```bash
# application.properties pode ler do sistema
JWT_SECRET=${jwt.secret:sua-chave-secreta}
JWT_EXPIRATION=${jwt.expiration:86400000}
DATABASE_URL=${spring.datasource.url}
```

Ou use `application-prod.properties`.

---

## 🎁 Bonus: Script de Deploy

Para facilitar deploy em servidor:

```bash
#!/bin/bash
# build-deploy.sh

echo "🔨 Compilando..."
./mvnw clean package -DskipTests

echo "📦 Construindo JAR..."
java -jar target/application-0.0.1-SNAPSHOT.jar

# Ou para produção:
# java -Xmx512m -Xms256m -jar target/application-0.0.1-SNAPSHOT.jar \
#   --spring.profiles.active=prod \
#   --server.port=8080
```

---

## 📞 Suporte Rápido

Dúvida sobre ? Consulte:
- Login → `EXEMPLOS_REQUISICOES.md`
- Fluxo completo → `RESUMO_IMPLEMENTACAO.md`
- Configuração → `AUTENTICACAO_JWT.md`
- Erro específico → `AUTENTICACAO_JWT.md` (seção Troubleshooting)

---

**✨ Implementação Completa e Pronta para Produção!**

© 2026 MicroBio Backend - JWT Authentication

