package com.chamei.pim4.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chamei.pim4.R;
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
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        if (!role.contains("admin")) {
            Ui.toast(this, "Apenas admin pode gerir usuários");
            finish();
            return;
        }

        usersApi = ApiClient.get(this).create(UsersApi.class);
        adapter = new UserAdapter(this);

        Toolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(this::showMenuFromIcon);

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        binding.btnAddUser.setOnClickListener(v -> showCreateDialog());

        loadUsers();
    }

    private boolean onMenuItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_dashboard) {
            startActivity(new android.content.Intent(this, HomeActivity.class));
            return true;
        }
        if (id == R.id.action_chamados) {
            startActivity(new android.content.Intent(this, ChamadosActivity.class));
            return true;
        }
        if (id == R.id.action_users) return true;
        if (id == R.id.action_profile) {
            startActivity(new android.content.Intent(this, ProfileActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            session.clear();
            Ui.toast(this, "Sessão encerrada");
            android.content.Intent i = new android.content.Intent(this, LoginActivity.class);
            i.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return false;
    }

    private void showMenuFromIcon(View anchor) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, anchor);
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
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        EditText nome = new EditText(this);
        nome.setHint("Nome");
        layout.addView(nome);

        EditText email = new EditText(this);
        email.setHint("Email");
        layout.addView(email);

        EditText senha = new EditText(this);
        senha.setHint("Senha");
        layout.addView(senha);

        EditText cpf = new EditText(this);
        cpf.setHint("CPF");
        layout.addView(cpf);

        Spinner cargo = new Spinner(this);
        ArrayAdapter<String> adapterCargo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"usuario", "Suporte", "admin"});
        cargo.setAdapter(adapterCargo);
        layout.addView(cargo);

        EditText nivel = new EditText(this);
        nivel.setHint("Nível (obrigatório para Suporte)");
        nivel.setVisibility(View.GONE);
        layout.addView(nivel);

        cargo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean isSuporte = "Suporte".equals(cargo.getSelectedItem());
                nivel.setVisibility(isSuporte ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

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
            String c = (String) cargo.getSelectedItem();
            String cpfVal = cpf.getText().toString().trim();
            boolean isSuporte = "Suporte".equalsIgnoreCase(c);
            if (n.isEmpty() || e.isEmpty() || s.isEmpty()) {
                Ui.toast(this, "Nome, email e senha são obrigatórios");
                return;
            }
            Integer nivelInt = null;
            if (isSuporte) {
                String nv = nivel.getText().toString().trim();
                if (nv.isEmpty()) {
                    Ui.toast(this, "Informe o nível para suporte");
                    return;
                }
                try {
                    nivelInt = Integer.parseInt(nv);
                } catch (NumberFormatException ex) {
                    Ui.toast(this, "Nível deve ser numérico");
                    return;
                }
            }
            dialog.dismiss();
            createUser(new CreateUserRequest(cpfVal, n, e, s, c, nivelInt));
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
