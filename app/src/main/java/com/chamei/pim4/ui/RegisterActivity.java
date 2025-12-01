package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.databinding.ActivityRegisterBinding;
import com.chamei.pim4.model.LoginResponse;
import com.chamei.pim4.model.RegisterRequest;
import com.chamei.pim4.network.AuthApi;
import com.chamei.pim4.util.Ui;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tela de criacao de conta: coleta dados basicos e chama API de registro.
 */
public class RegisterActivity extends AppCompatActivity {

    // Binding do layout para acessar os campos sem findViewById
    private ActivityRegisterBinding binding;
    // Cliente de autenticacao/registro
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Instancia o cliente Retrofit
        authApi = ApiClient.get(this).create(AuthApi.class);

        // Liga botoes aos handlers
        binding.btnRegister.setOnClickListener(v -> doRegister());
        binding.linkBackLogin.setOnClickListener(v -> finish());
    }

    private void setLoading(boolean loading) {
        // Exibe loading e evita cliques repetidos
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }

    private void doRegister() {
        // Le dados informados pelo usuario
        String cpf = binding.inputCpf.getText() != null ? binding.inputCpf.getText().toString().trim() : "";
        String nome = binding.inputNome.getText() != null ? binding.inputNome.getText().toString().trim() : "";
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String pass = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString() : "";

        if (cpf.isEmpty() || nome.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Ui.toast(this, "Todos os campos sao obrigatorios");
            return;
        }

        // Envia dados para API de registro
        Ui.hideKeyboard(this);
        setLoading(true);
        Call<LoginResponse> call = authApi.register(new RegisterRequest(cpf, nome, email, pass));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Cadastro OK: volta para tela de login limpando pilha
                    Ui.toast(RegisterActivity.this, "Conta criada! Faca login para continuar");
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // Exibe mensagem amigavel vinda do servidor
                    String msg = ApiClient.errorConverter().toMessage(response.errorBody(), "Falha ao registrar");
                    Ui.toast(RegisterActivity.this, msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                // Erro de rede ou inesperado
                Ui.toast(RegisterActivity.this, "Erro ao conectar: " + t.getMessage());
            }
        });
    }
}
