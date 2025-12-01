package com.chamei.pim4.model;

/**
 * Payload enviado ao endpoint de login.
 */
public class LoginRequest {
    // Credenciais informadas na tela de login
    public String email;
    public String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
