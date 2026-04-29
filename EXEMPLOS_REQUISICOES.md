# 🧪 Exemplos de Requisições - MicroBio Backend JWT API

Use estes exemplos para testar a autenticação. Você pode usar:
- **Postman** (GUI)
- **cURL** (terminal)
- **Thunder Client** (VSCode)
- **REST Client** (VSCode)
- **Fetch API** (JavaScript)

---

## 1️⃣ LOGIN - Obter Token JWT

### cURL
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### PowerShell
```powershell
$json = @{username='admin'; password='admin123'} | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $json
$response.Content | ConvertFrom-Json | ForEach-Object { Write-Host "Token: $($_.token)" }
```

### JavaScript/Fetch
```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
});

const data = await response.json();
console.log('Token:', data.token);
console.log('Role:', data.role);

// Armazenar token
localStorage.setItem('token', data.token);
```

### Resposta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "username": "admin",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjoiUk9MRV9BRE1JTiIsImlhdCI6MTcxNzA3MTgyMCwiZXhwIjoxNzE3MTU4MjIwfQ.v4WzfC6gR-i4QTWl3eQ4kZ0U1cjoVD5C4IhYbUlRkcWxkvuaGpkxGkkcn-nyOv5hXfIIjfbIa123h0Hagz5tIg",
  "role": "ADMIN"
}
```

---

## 2️⃣ LOGIN COM USUÁRIO COMUM

### cURL
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123"
  }'
```

### Resposta (200 OK)
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "username": "user",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "role": "USER"
}
```

---

## 3️⃣ LOGIN COM CREDENCIAIS INVÁLIDAS

### cURL
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "senhaerrada"
  }'
```

### Resposta Esperada (401 Unauthorized)
```json
{
  "success": false,
  "message": "Usuário ou senha incorretos",
  "username": null,
  "token": null,
  "role": null
}
```

---

## 4️⃣ OBTER INFORMAÇÕES DO USUÁRIO AUTENTICADO

### cURL
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### PowerShell
```powershell
$token = "eyJhbGciOiJIUzUxMiJ9..."
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/me" `
  -Headers @{"Authorization"="Bearer $token"}
$response.Content | ConvertFrom-Json
```

### JavaScript/Fetch
```javascript
const token = localStorage.getItem('token');
const response = await fetch('http://localhost:8080/api/auth/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
console.log(data);
```

### Resposta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Usuário autenticado",
  "username": "admin",
  "token": null,
  "role": "ADMIN"
}
```

---

## 5️⃣ CHAMAR ENDPOINT PROTEGIDO SEM TOKEN

### cURL
```bash
curl -X GET http://localhost:8080/api/auth/me
```

### Resposta Esperada (403 Forbidden)
```
<HTML><HEAD><TITLE>403 - Forbidden</TITLE></HEAD><BODY>...</BODY></HTML>
```

---

## 6️⃣ LOGOUT

### cURL
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json"
```

### JavaScript/Fetch
```javascript
const response = await fetch('http://localhost:8080/api/auth/logout', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
console.log(data);

// Limpar armazenamento local
localStorage.removeItem('token');
localStorage.removeItem('role');
```

### Resposta (200 OK)
```json
{
  "success": true,
  "message": "Logout realizado com sucesso",
  "username": null,
  "token": null,
  "role": null
}
```

---

## 7️⃣ USAR TOKEN EM OUTRAS REQUISIÇÕES (exemplo)

Assumindo que você tem endpoints como:
- `GET /api/servicos`
- `POST /api/orcamentos`
- etc.

### cURL
```bash
TOKEN="seu_token_aqui"

# GET
curl -X GET http://localhost:8080/api/servicos \
  -H "Authorization: Bearer $TOKEN"

# POST
curl -X POST http://localhost:8080/api/orcamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "descricao": "Novo orçamento", ... }'
```

### JavaScript/Fetch
```javascript
async function apiCall(endpoint, method = 'GET', body = null) {
  const token = localStorage.getItem('token');
  
  const options = {
    method: method,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };
  
  if (body) {
    options.body = JSON.stringify(body);
  }
  
  const response = await fetch(`http://localhost:8080${endpoint}`, options);
  
  if (response.status === 403) {
    console.log('Token expirado - faça novo login');
    localStorage.clear();
    // Redirecionar para login
  }
  
  return response.json();
}

// Usar:
const servicos = await apiCall('/api/servicos');
const novoOrcamento = await apiCall('/api/orcamentos', 'POST', {
  descricao: 'Novo orçamento'
});
```

---

## 📋 Arquivo .rest (REST Client VSCode)

Crie arquivo `test-api.rest` na raiz do projeto:

```rest
@host = http://localhost:8080
@token = eyJhbGciOiJIUzUxMiJ9...

### Login Admin
POST {{host}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

###

### Login User
POST {{host}}/api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "user123"
}

###

### Get Current User
GET {{host}}/api/auth/me
Authorization: Bearer {{token}}

###

### Logout
POST {{host}}/api/auth/logout

###

### Get Servicos (protegidc)
GET {{host}}/api/servicos
Authorization: Bearer {{token}}

###

### Get Orcamentos (protegido)
GET {{host}}/api/orcamentos
Authorization: Bearer {{token}}

###
```

Depois use Ctrl+Alt+R em cada bloco para executar.

---

## 🧩 Coleção Postman JSON

Cole isto em Postman `File → Import → Raw text`:

```json
{
  "info": {
    "name": "MicroBio Backend JWT",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Login Admin",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"username\": \"admin\", \"password\": \"admin123\"}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Get Current User",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwt_token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/auth/me",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "me"]
            }
          }
        },
        {
          "name": "Logout",
          "request": {
            "method": "POST",
            "url": {
              "raw": "{{base_url}}/api/auth/logout",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "logout"]
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    },
    {
      "key": "jwt_token",
      "value": ""
    }
  ]
}
```

Adicione variável no Postman:
- `base_url` = `http://localhost:8080`
- `jwt_token` = (salvar token após login)

---

## ✅ Testes Recomendados

Siga esta ordem para validar a implementação:

```
1. POST /api/auth/login (admin)
   ├─ ✓ Status 200
   ├─ ✓ Token gerado
   └─ ✓ Role = ADMIN

2. GET /api/auth/me (com token)
   ├─ ✓ Status 200
   ├─ ✓ Username retornado
   └─ ✓ Role retornado

3. GET /api/auth/me (sem token)
   └─ ✓ Status 403

4. POST /api/auth/login (user)
   └─ ✓ Token diferente do admin

5. POST /api/auth/login (credenciais erradas)
   ├─ ✓ Status 401
   └─ ✓ Mensagem de erro

6. POST /api/auth/logout
   └─ ✓ Status 200
```

---

## 🐛 Debug

### Ver payload do token JWT

Acesse https://jwt.io e cole seu token para decodificar.

O token não é criptografado, apenas assinado, então você verá:

```json
{
  "sub": "admin",
  "roles": "ROLE_ADMIN",
  "iat": 1717071820,
  "exp": 1717158220
}
```

Onde:
- `sub` = subject (username)
- `roles` = autoridades do usuário
- `iat` = issued at (quando foi gerado)
- `exp` = expiration (quando expira)

---

## 🔒 Segurança nas Requisições

#### ✅ Correto
```bash
curl -H "Authorization: Bearer eyJhbGc..."
```

#### ❌ Errado
```bash
curl -H "Authorization: eyJhbGc..."  # Falta "Bearer "
curl -H "Authorization: Token eyJhbGc..."  # Token ao invés de Bearer
curl -H "authorization: Bearer eyJhbGc..."  # Minúsculo (case sensitive em alguns casos)
```

---

**Fim de Exemplos - Bom teste!** 🎉

