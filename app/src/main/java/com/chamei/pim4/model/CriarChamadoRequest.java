package com.chamei.pim4.model;

public class CriarChamadoRequest {
    public String titulo;
    public String motivo;
    public String descricao;
    public Integer prioridade;

    public CriarChamadoRequest(String titulo, String motivo, String descricao, Integer prioridade) {
        this.titulo = titulo;
        this.motivo = motivo;
        this.descricao = descricao;
        this.prioridade = prioridade;
    }
}
