# Payable Management API

## Segurança

> A API usa HTTP Basic Authentication como proteção simples para o desafio técnico. Em produção, essa camada seria substituída por OAuth2/OIDC com um provedor de identidade e HTTPS obrigatório.

Por padrão, a aplicação usa `totvs` como usuário e senha. As credenciais podem ser sobrescritas antes da inicialização:

```bash
export APP_SECURITY_USERNAME=totvs
export APP_SECURITY_PASSWORD=change-me
```

Exemplo de chamada autenticada:

```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" \
  http://localhost:8080/management/rest/payable
```
