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
    // Instancia unica de Retrofit compartilhada pela app
    private static Retrofit retrofit;
    // Conversor para extrair mensagens de erro HTTP em texto
    private static ApiErrorConverter errorConverter;

    /**
     * Cria (ou devolve) o cliente Retrofit configurado com interceptores de auth/log
     * e conversor Gson preparado para datas no formato da API.
     * Mantem uma unica instancia global para reaproveitar conexoes HTTP.
     */
    public static synchronized Retrofit get(Context context) {
        // Reutiliza a instancia se ja foi criada para evitar clientes duplicados
        if (retrofit != null) return retrofit;

        // SessionManager fornece o token salvo para ser anexado pelo interceptor
        SessionManager session = new SessionManager(context.getApplicationContext());
        // Interceptor de log para facilitar depuracao das chamadas HTTP
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Cliente OkHttp com interceptor de autenticacao e logs
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(session))
                .addInterceptor(logging)
                .build();

        // Gson configurado para desserializar datas no formato da API
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        // Construcao do Retrofit apontando para a URL vinda do .env/BuildConfig
        retrofit = new Retrofit.Builder()
                .baseUrl(EnvConfig.getApiBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Conversor usado para exibir mensagens de erro amigaveis
        errorConverter = new ApiErrorConverter(retrofit.responseBodyConverter(Object.class, new java.lang.annotation.Annotation[0]));
        return retrofit;
    }

    public static ApiErrorConverter errorConverter() {
        // Exposto para atividades criarem mensagens de erro padronizadas
        return errorConverter;
    }
}
