package com.chamei.pim4.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Wrapper para armazenar e recuperar dados de sessao via SharedPreferences.
 * Centraliza token e informacoes basicas do usuario logado.
 */
public class SessionManager {
    // Nome do arquivo de SharedPreferences usado pela app
    private static final String PREFS = "pim4_session";
    // Chaves de armazenamento de dados do usuario
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        // Inicializa o SharedPreferences privado para a app
        this.prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        // Grava o bearer token retornado no login
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        // Recupera token para uso no interceptor
        return prefs.getString(KEY_TOKEN, "");
    }

    public void saveUser(String name, String email, String role, int id) {
        // Persiste informacoes basicas do usuario autenticado
        prefs.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .putInt(KEY_USER_ID, id)
                .apply();
    }

    // Getters simples usados pela UI e interceptors
    public String getUserName() { return prefs.getString(KEY_USER_NAME, ""); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public String getUserRole() { return prefs.getString(KEY_USER_ROLE, ""); }
    public int getUserId() { return prefs.getInt(KEY_USER_ID, 0); }

    public boolean isAuthenticated() {
        // Considera autenticado apenas se ha token salvo
        return !TextUtils.isEmpty(getToken());
    }

    public void clear() {
        // Limpa todos os dados persistidos ao fazer logout
        prefs.edit().clear().apply();
    }
}
