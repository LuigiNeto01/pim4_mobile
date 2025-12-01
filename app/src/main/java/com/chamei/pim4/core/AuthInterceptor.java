package com.chamei.pim4.core;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor responsavel por incluir o header Authorization em cada requisicao
 * quando existir um token salvo no SessionManager.
 */
public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Insere o bearer token no cabecalho antes de prosseguir com a cadeia.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            // Sem token: deixa a chamada seguir sem autenticacao
            return chain.proceed(original);
        }

        // Reconstrui a requisicao adicionando o bearer token
        Request authed = original.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authed);
    }
}
