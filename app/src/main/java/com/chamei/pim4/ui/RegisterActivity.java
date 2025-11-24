package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityRegisterBinding;
import com.chamei.pim4.model.LoginResponse;
import com.chamei.pim4.model.RegisterRequest;
import com.chamei.pim4.network.AuthApi;
import com.chamei.pim4.util.Ui;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthApi authApi;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        authApi = ApiClient.get(this).create(AuthApi.class);

        binding.btnRegister.setOnClickListener(v -> doRegister());
        binding.linkBackLogin.setOnClickListener(v -> finish());
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }

    private void doRegister() {
        String cpf = binding.inputCpf.getText() != null ? binding.inputCpf.getText().toString().trim() : "";
        String nome = binding.inputNome.getText() != null ? binding.inputNome.getText().toString().trim() : "";
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String pass = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString() : "";

        if (nome.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Ui.toast(this, "Preencha nome, email e senha");
            return;
        }

        Ui.hideKeyboard(this);
        setLoading(true);
        Call<LoginResponse> call = authApi.register(new RegisterRequest(cpf, nome, email, pass));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    session.saveToken(res.token);
                    if (res.user != null) {
                        session.saveUser(res.user.nome, res.user.email, res.user.cargo, res.user.id);
                    }
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    String msg = ApiClient.errorConverter().toMessage(response.errorBody(), "Falha ao registrar");
                    Ui.toast(RegisterActivity.this, msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(RegisterActivity.this, "Erro ao conectar: " + t.getMessage());
            }
        });
    }
}
