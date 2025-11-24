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

public interface ChamadosApi {
    @GET("/chamados")
    Call<List<Chamado>> listar();

    @POST("/chamados/by-user")
    Call<List<Chamado>> listarPorUsuario(@Body Map<String, Integer> body);

    @POST("/chamados")
    Call<Map<String, Object>> criar(@Body CriarChamadoRequest body);

    @PUT("/chamados/{id}/close")
    Call<Map<String, Object>> fechar(@Path("id") int id);

    @PUT("/chamados/{id}/reopen")
    Call<Map<String, Object>> reabrir(@Path("id") int id);
}
