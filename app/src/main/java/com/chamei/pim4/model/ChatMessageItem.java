package com.chamei.pim4.model;

import java.util.Date;

/**
 * Modela cada mensagem exibida no chat de um chamado.
 */
public class ChatMessageItem {
    // Identificador da mensagem
    public int id;
    // Chamado ao qual a mensagem pertence
    public int idChamado;
    // Usuario que enviou
    public int idUsuario;
    // Nome de exibicao do autor
    public String nome;
    // Conteudo da mensagem
    public String mensagem;
    // Data/hora do envio
    public Date dataEnvio;
}
