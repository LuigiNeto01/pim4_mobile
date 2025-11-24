package com.chamei.pim4.model;

public class RegisterRequest {
    public String cpf;
    public String nome;
    public String email;
    public String password;

    public RegisterRequest(String cpf, String nome, String email, String password) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.password = password;
    }
}
