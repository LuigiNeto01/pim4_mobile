package com.chamei.pim4.model;

/**
 * Corpo usado pelo admin para criar novos usuarios.
 */
public class CreateUserRequest {
    // Dados enviados pelo admin para criar usuarios
    public String cpf;
    public String nome;
    public String email;
    public String senha;
    public String cargo;
    public Integer nivel;

    public CreateUserRequest(String cpf, String nome, String email, String senha, String cargo, Integer nivel) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cargo = cargo;
        this.nivel = nivel;
    }
}
