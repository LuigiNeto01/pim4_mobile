package com.chamei.pim4.core;

import android.content.Context;

import com.chamei.pim4.core.network.ApiErrorConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fabrica singletons de Retrofit/OkHttp usando base URL do .env.
 */
public class ApiClient {
    private static Retrofit retrofit;
    private static ApiErrorConverter errorConverter;

    public static synchronized Retrofit get(Context context) {
        if (retrofit != null) return retrofit;

        SessionManager session = new SessionManager(context.getApplicationContext());
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(session))
                .addInterceptor(logging)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(EnvConfig.getApiBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        errorConverter = new ApiErrorConverter(retrofit.responseBodyConverter(Object.class, new java.lang.annotation.Annotation[0]));
        return retrofit;
    }

    public static ApiErrorConverter errorConverter() {
        return errorConverter;
    }
}
