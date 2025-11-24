package com.chamei.pim4.network;

import com.chamei.pim4.model.LoginRequest;
import com.chamei.pim4.model.LoginResponse;
import com.chamei.pim4.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest body);
}
