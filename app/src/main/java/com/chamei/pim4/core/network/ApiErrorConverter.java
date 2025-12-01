package com.chamei.pim4.core.network;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Converte o corpo de erro das respostas HTTP em uma mensagem de texto amigavel.
 * Funciona para respostas JSON que trazem a chave "message".
 */
public class ApiErrorConverter {
    private final Converter<ResponseBody, Object> converter;

    public ApiErrorConverter(Converter<ResponseBody, Object> converter) {
        this.converter = converter;
    }

    /**
     * Tenta extrair uma mensagem legivel do corpo de erro devolvido pelo Retrofit.
     * Prioriza a chave "message" quando a API segue o padrao { "message": "..." }.
     */
    public String toMessage(ResponseBody body, String fallback) {
        // Se nao houver corpo, devolve a mensagem padrao informada
        if (body == null) return fallback;
        try {
            Object obj = converter.convert(body);
            // Quando o retorno eh um mapa, tenta extrair a chave "message"
            if (obj instanceof java.util.Map) {
                Object msg = ((java.util.Map<?, ?>) obj).get("message");
                if (msg != null) return msg.toString();
            }
            // Fallback: usa toString do objeto ou a mensagem default
            return obj != null ? obj.toString() : fallback;
        } catch (IOException e) {
            return fallback;
        }
    }
}
