package com.chamei.pim4.network;

import com.chamei.pim4.model.AiOpinionResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiApi {
    @POST("/ai/chamado/opiniao")
    Call<AiOpinionResponse> opiniaoChamado(@Body Map<String, Object> body);
}
