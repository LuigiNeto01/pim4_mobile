package com.chamei.pim4.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.chamei.pim4.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Le variaveis de ambiente do arquivo .env (assets) e expõe helpers de configuracao.
 * Útil para alinhar a URL da API com o front web.
 */
public final class EnvConfig {
    private static final String ENV_ASSET_NAME = ".env";
    private static final Map<String, String> values = new HashMap<>();
    private static boolean initialized = false;

    private EnvConfig() {}

    public static synchronized void init(Context context) {
        if (initialized) return;
        // Defaults vindos do BuildConfig para não depender do .env
        values.put("API_BASE_URL", safe(BuildConfig.API_BASE_URL));
        values.put("DEBUG_LOGIN", Boolean.toString(BuildConfig.DEBUG_LOGIN));

        AssetManager assets = context.getAssets();
        try (InputStream is = assets.open(ENV_ASSET_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx <= 0) continue;
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                if (!TextUtils.isEmpty(key)) values.put(key, val);
            }
        } catch (IOException ignored) {
            // Se o arquivo não existir, seguimos com os defaults
        }
        initialized = true;
    }

    public static String get(String key, String fallback) {
        String val = values.get(key);
        if (TextUtils.isEmpty(val)) return fallback;
        return val;
    }

    public static String getApiBaseUrl() {
        return get("API_BASE_URL", "http://10.0.2.2:5132");
    }

    public static boolean isDebugLogin() {
        return Boolean.parseBoolean(get("DEBUG_LOGIN", Boolean.toString(BuildConfig.DEBUG_LOGIN)));
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}
