package com.chamei.pim4.model;

import java.util.Date;

/**
 * Entidade de chamado (ticket) manipulada no app.
 */
public class Chamado {
    // Identificador do chamado
    public int id;
    // Titulo curto do problema relatado
    public String titulo;
    // Motivo selecionado ao abrir o chamado
    public String motivo;
    // Descricao detalhada (obrigatoria para "Outros")
    public String descricao;
    // Nivel de prioridade (1 critica a 4 baixa)
    public int prioridade;
    // Flag que indica se ja foi resolvido/fechado
    public boolean resolvido;
    // Data em que o chamado foi criado
    public Date dataCriacao;
    // Id do usuario que criou
    public Integer usuarioCriadorId;
    // Nome do criador para exibicao no card
    public String nomeCriador;
}
