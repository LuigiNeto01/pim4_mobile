package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.core.EnvConfig;
import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityLoginBinding;
import com.chamei.pim4.model.LoginRequest;
import com.chamei.pim4.model.LoginResponse;
import com.chamei.pim4.network.AuthApi;
import com.chamei.pim4.util.Ui;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tela inicial de login: valida campos, chama a API e salva sessao local.
 */
public class LoginActivity extends AppCompatActivity {

    // Binding gerado para acessar as views do layout
    private ActivityLoginBinding binding;
    // Cliente da API de autenticacao
    private AuthApi authApi;
    // Gerenciador de sessao/local storage
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Prepara dependencias
        session = new SessionManager(this);
        authApi = ApiClient.get(this).create(AuthApi.class);

        // Acoes dos botoes da tela
        binding.btnLogin.setOnClickListener(v -> doLogin());
        binding.btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void setLoading(boolean loading) {
        // Mostra barra e desabilita botao para evitar clique duplo
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }

    private void doLogin() {
        // Coleta valores dos campos
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String pass = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString() : "";
        if (email.isEmpty() || pass.isEmpty()) {
            Ui.toast(this, "Informe email e senha");
            return;
        }

        // Esconde teclado e dispara chamada de login
        Ui.hideKeyboard(this);
        setLoading(true);
        Call<LoginResponse> call = authApi.login(new LoginRequest(email, pass));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Salva token e dados do usuario e segue para home
                    LoginResponse res = response.body();
                    session.saveToken(res.token);
                    if (res.user != null) {
                        session.saveUser(res.user.nome, res.user.email, res.user.cargo, res.user.id);
                    }
                    openHome();
                } else {
                    // Mostra mensagem de erro retornada pela API
                    String msg = ApiClient.errorConverter().toMessage(response.errorBody(), "Falha no login");
                    Ui.toast(LoginActivity.this, msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                // Erro de rede ou excecao
                Ui.toast(LoginActivity.this, "Erro ao conectar: " + t.getMessage());
            }
        });
    }

    private void openHome() {
        // Limpa o back stack para impedir voltar para login depois de logado
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
