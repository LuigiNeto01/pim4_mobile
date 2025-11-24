package com.chamei.pim4.model;

import java.util.Date;

public class Chamado {
    public int id;
    public String titulo;
    public String motivo;
    public String descricao;
    public int prioridade;
    public boolean resolvido;
    public Date dataCriacao;
    public Integer usuarioCriadorId;
    public String nomeCriador;
}
