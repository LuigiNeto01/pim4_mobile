package com.chamei.pim4.network;

import com.chamei.pim4.model.Chamado;
import com.chamei.pim4.model.CriarChamadoRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Contrato Retrofit para operacoes com chamados.
 */
public interface ChamadosApi {
    // Lista todos os chamados existentes
    @GET("/chamados")
    Call<List<Chamado>> listar();

    // Lista chamados do usuario autenticado (quando perfil eh usuario)
    @POST("/chamados/by-user")
    Call<List<Chamado>> listarPorUsuario(@Body Map<String, Integer> body);

    // Cria um novo chamado
    @POST("/chamados")
    Call<Map<String, Object>> criar(@Body CriarChamadoRequest body);

    // Marca chamado como fechado/resolvido
    @PUT("/chamados/{id}/close")
    Call<Map<String, Object>> fechar(@Path("id") int id);

    // Reabre um chamado fechado
    @PUT("/chamados/{id}/reopen")
    Call<Map<String, Object>> reabrir(@Path("id") int id);
}
