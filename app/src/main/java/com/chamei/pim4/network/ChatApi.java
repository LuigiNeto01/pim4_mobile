package com.chamei.pim4.network;

import com.chamei.pim4.model.ChatMessageItem;
import com.chamei.pim4.model.SendChatMessageRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Endpoints relacionados ao chat de um chamado.
 */
public interface ChatApi {
    // Busca historico de mensagens de um chamado especifico
    @GET("/chat/{chamadoId}/messages")
    Call<List<ChatMessageItem>> listarMensagens(@Path("chamadoId") int chamadoId);

    // Envia nova mensagem para um chamado
    @POST("/chat/{chamadoId}/messages")
    Call<Map<String, Object>> enviar(@Path("chamadoId") int chamadoId, @Body SendChatMessageRequest body);
}
