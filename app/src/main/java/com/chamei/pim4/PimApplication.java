package com.chamei.pim4;

import android.app.Application;

import com.chamei.pim4.core.EnvConfig;

/**
 * Ponto de entrada global: inicializa configuracoes do .env ao subir a app.
 */
public class PimApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa leitura do .env para configurar URL base e flags
        EnvConfig.init(this);
    }
}
