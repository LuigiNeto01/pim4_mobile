package com.chamei.pim4.network;

import com.chamei.pim4.model.CreateUserRequest;
import com.chamei.pim4.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Endpoints de administracao de usuarios.
 */
public interface UsersApi {
    // Lista todos os usuarios
    @GET("/users")
    Call<List<User>> list();

    // Cria um novo usuario
    @POST("/users")
    Call<Map<String, Object>> create(@Body CreateUserRequest body);

    // Atualiza um usuario pelo ID (admin)
    @PUT("/users/{id}")
    Call<Map<String, Object>> update(@Path("id") int id, @Body Map<String, Object> body);

    // Atualiza o usuario logado
    @PUT("/users/me")
    Call<Map<String, Object>> updateMe(@Body Map<String, Object> body);

    // Remove usuario pelo ID
    @DELETE("/users/{id}")
    Call<Map<String, Object>> delete(@Path("id") int id);
}
