package com.chamei.pim4.model;

/**
 * Representa um usuario retornado/aceito pela API.
 */
public class User {
    // Identificador unico do usuario
    public int id;
    // Documento CPF (sem formatacao)
    public String cpf;
    // Nome completo para exibicao
    public String nome;
    // Email usado no login/comunicacao
    public String email;
    // Cargo (admin, suporte, usuario)
    public String cargo;
    // Nivel de suporte (apenas para cargos de suporte)
    public Integer nivel;
}
