package com.chamei.pim4.model;

/**
 * Corpo enviado para postar uma nova mensagem no chat.
 */
public class SendChatMessageRequest {
    // Conteudo da mensagem enviada no chat do chamado
    public String mensagem;

    public SendChatMessageRequest(String mensagem) {
        this.mensagem = mensagem;
    }
}
