package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chamei.pim4.R;
import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityProfileBinding;
import com.chamei.pim4.network.UsersApi;
import com.chamei.pim4.util.Ui;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager session;
    private UsersApi usersApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        if (!session.isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        usersApi = ApiClient.get(this).create(UsersApi.class);

        Toolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);
        if (isAdmin()) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(this::showMenu);
        } else {
            toolbar.setNavigationIcon(null);
        }
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        binding.inputNome.setText(session.getUserName());
        binding.inputEmail.setText(session.getUserEmail());
        binding.txtCargo.setText("Cargo: " + session.getUserRole());

        binding.btnSalvar.setOnClickListener(v -> saveProfile());
    }

    private boolean onMenuItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_dashboard) {
            startActivity(new Intent(this, HomeActivity.class));
            return true;
        }
        if (id == R.id.action_chamados) {
            startActivity(new Intent(this, ChamadosActivity.class));
            return true;
        }
        if (id == R.id.action_users) {
            if (isAdmin()) {
                startActivity(new Intent(this, UsersActivity.class));
            } else {
                Ui.toast(this, "Apenas admin pode gerir usuários");
            }
            return true;
        }
        if (id == R.id.action_profile) return true;
        if (id == R.id.action_logout) {
            session.clear();
            Ui.toast(this, "Sessão encerrada");
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return false;
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.inflate(R.menu.menu_home);
        menu.setOnMenuItemClickListener(this::onMenuItemClick);
        menu.show();
    }

    private boolean isAdmin() {
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        return role.contains("admin");
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSalvar.setEnabled(!loading);
    }

    private void saveProfile() {
        String nome = binding.inputNome.getText() != null ? binding.inputNome.getText().toString().trim() : "";
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String senha = binding.inputSenha.getText() != null ? binding.inputSenha.getText().toString() : "";

        if (nome.isEmpty() || email.isEmpty()) {
            Ui.toast(this, "Nome e email são obrigatórios");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("email", email);
        if (!senha.isEmpty()) body.put("senha", senha);

        setLoading(true);
        usersApi.update(session.getUserId(), body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    session.saveUser(nome, email, session.getUserRole(), session.getUserId());
                    Ui.toast(ProfileActivity.this, "Perfil atualizado");
                    finish();
                } else {
                    Ui.toast(ProfileActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao atualizar"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(ProfileActivity.this, "Falha: " + t.getMessage());
            }
        });
    }
}
