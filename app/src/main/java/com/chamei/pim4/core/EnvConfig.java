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
 * Le variaveis de ambiente do arquivo .env (armazenado em assets) e deixa os valores
 * acessiveis estaticamente. Serve para alinhar URL base da API ou flags entre devs.
 */
public final class EnvConfig {
    // Nome do arquivo que contem as variaveis no pacote de assets
    private static final String ENV_ASSET_NAME = ".env";
    // Cache em memoria das chaves/valores carregados
    private static final Map<String, String> values = new HashMap<>();
    // Evita inicializar a cada chamada
    private static boolean initialized = false;

    private EnvConfig() {}

    /**
     * Inicializa leitura do .env e popula o cache de variaveis.
     * Deve ser chamado apenas uma vez (PimApplication) antes de usar get().
     */
    public static synchronized void init(Context context) {
        // Se ja carregou uma vez, nao faz nada
        if (initialized) return;
        // Defaults vindos do BuildConfig para nao depender obrigatoriamente do .env
        values.put("API_BASE_URL", safe(BuildConfig.API_BASE_URL));
        values.put("DEBUG_LOGIN", Boolean.toString(BuildConfig.DEBUG_LOGIN));

        // Tenta ler o arquivo .env linha a linha no formato CHAVE=VALOR
        AssetManager assets = context.getAssets();
        try (InputStream is = assets.open(ENV_ASSET_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignora comentarios ou linhas vazias
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx <= 0) continue;
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                if (!TextUtils.isEmpty(key)) values.put(key, val);
            }
        } catch (IOException ignored) {
            // Se o arquivo nao existir, seguimos apenas com os defaults
        }
        initialized = true;
    }

    public static String get(String key, String fallback) {
        // Retorna o valor salvo ou o fallback informado
        String val = values.get(key);
        if (TextUtils.isEmpty(val)) return fallback;
        return val;
    }

    /** URL principal utilizada pelo Retrofit */
    public static String getApiBaseUrl() {
        // URL que o Retrofit deve usar como base
        return get("API_BASE_URL", "http://10.0.2.2:5132");
    }

    /** Flag de atalho para habilitar login de debug/local */
    public static boolean isDebugLogin() {
        // Indica se recursos de debug de login estao ativados
        return Boolean.parseBoolean(get("DEBUG_LOGIN", Boolean.toString(BuildConfig.DEBUG_LOGIN)));
    }

    private static String safe(String v) {
        // Trata strings nulas para evitar NullPointer
        return v == null ? "" : v;
    }
}
