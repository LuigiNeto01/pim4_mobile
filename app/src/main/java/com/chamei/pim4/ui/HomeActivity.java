package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityHomeBinding;
import com.chamei.pim4.util.Ui;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        if (!session.isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.txtUserName.setText(session.getUserName().isEmpty() ? "Usuário" : session.getUserName());
        binding.txtUserEmail.setText(session.getUserEmail());
        binding.txtUserRole.setText(session.getUserRole());

        binding.cardChamados.setOnClickListener(v -> startActivity(new Intent(this, ChamadosActivity.class)));
        binding.cardChat.setOnClickListener(v -> startActivity(new Intent(this, ChamadosActivity.class)));
        binding.cardUsers.setOnClickListener(v -> startActivity(new Intent(this, UsersActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            session.clear();
            Ui.toast(this, "Sessão encerrada");
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
