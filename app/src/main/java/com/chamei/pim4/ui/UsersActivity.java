package com.chamei.pim4.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityUsersBinding;
import com.chamei.pim4.model.CreateUserRequest;
import com.chamei.pim4.model.User;
import com.chamei.pim4.network.UsersApi;
import com.chamei.pim4.ui.adapters.UserAdapter;
import com.chamei.pim4.util.Ui;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity implements UserAdapter.Listener {

    private ActivityUsersBinding binding;
    private UsersApi usersApi;
    private UserAdapter adapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        usersApi = ApiClient.get(this).create(UsersApi.class);
        adapter = new UserAdapter(this);

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddUser.setOnClickListener(v -> showCreateDialog());

        loadUsers();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadUsers() {
        setLoading(true);
        usersApi.list().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Ui.toast(UsersActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao listar"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(UsersActivity.this, "Falha: " + t.getMessage());
            }
        });
    }

    private void showCreateDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        EditText nome = new EditText(this); nome.setHint("Nome");
        EditText email = new EditText(this); email.setHint("Email");
        EditText senha = new EditText(this); senha.setHint("Senha");
        EditText cargo = new EditText(this); cargo.setHint("Cargo (admin/usuario/suporte)");
        EditText nivel = new EditText(this); nivel.setHint("Nível (opcional)");

        layout.addView(nome);
        layout.addView(email);
        layout.addView(senha);
        layout.addView(cargo);
        layout.addView(nivel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Novo usuário")
                .setView(layout)
                .setPositiveButton("Criar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n = nome.getText().toString().trim();
            String e = email.getText().toString().trim();
            String s = senha.getText().toString();
            String c = cargo.getText().toString().trim();
            if (n.isEmpty() || e.isEmpty() || s.isEmpty()) {
                Ui.toast(this, "Nome, email e senha são obrigatórios");
                return;
            }
            Integer nivelInt = null;
            try {
                String nv = nivel.getText().toString().trim();
                if (!nv.isEmpty()) nivelInt = Integer.parseInt(nv);
            } catch (NumberFormatException ignored) {}
            dialog.dismiss();
            createUser(new CreateUserRequest("", n, e, s, c.isEmpty() ? "usuario" : c, nivelInt));
        }));
        dialog.show();
    }

    private void createUser(CreateUserRequest req) {
        setLoading(true);
        usersApi.create(req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Ui.toast(UsersActivity.this, "Usuário criado");
                    loadUsers();
                } else {
                    Ui.toast(UsersActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao criar usuário"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(UsersActivity.this, "Falha: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Remover usuário")
                .setMessage("Remover " + user.nome + "?")
                .setPositiveButton("Remover", (d, w) -> doDelete(user.id))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void doDelete(int id) {
        setLoading(true);
        usersApi.delete(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Ui.toast(UsersActivity.this, "Removido");
                    loadUsers();
                } else {
                    Ui.toast(UsersActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao remover"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(UsersActivity.this, "Falha: " + t.getMessage());
            }
        });
    }
}
