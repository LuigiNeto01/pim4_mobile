package com.chamei.pim4.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SessionManager {
    private static final String PREFS = "pim4_session";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        this.prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public void saveUser(String name, String email, String role, int id) {
        prefs.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .putInt(KEY_USER_ID, id)
                .apply();
    }

    public String getUserName() { return prefs.getString(KEY_USER_NAME, ""); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public String getUserRole() { return prefs.getString(KEY_USER_ROLE, ""); }
    public int getUserId() { return prefs.getInt(KEY_USER_ID, 0); }

    public boolean isAuthenticated() {
        return !TextUtils.isEmpty(getToken());
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
