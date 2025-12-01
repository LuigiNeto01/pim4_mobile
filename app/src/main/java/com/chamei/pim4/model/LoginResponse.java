package com.chamei.pim4.model;

import java.util.Date;

/**
 * Resposta recebida apos autenticacao.
 */
public class LoginResponse {
    // Token JWT/Bearer retornado pela API
    public String token;
    // Expiracao do token
    public Date expiresAt;
    // Dados do usuario autenticado
    public User user;
}
