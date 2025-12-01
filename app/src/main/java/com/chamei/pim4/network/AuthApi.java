package com.chamei.pim4.network;

import com.chamei.pim4.model.LoginRequest;
import com.chamei.pim4.model.LoginResponse;
import com.chamei.pim4.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Endpoints de autenticacao/registro.
 */
public interface AuthApi {
    // Autentica usuario e devolve token + dados do usuario
    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    // Cria um novo usuario e ja retorna token para login automatico
    @POST("/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest body);
}
