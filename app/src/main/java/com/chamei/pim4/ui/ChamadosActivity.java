package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityChamadosBinding;
import com.chamei.pim4.model.Chamado;
import com.chamei.pim4.model.CriarChamadoRequest;
import com.chamei.pim4.network.ChamadosApi;
import com.chamei.pim4.ui.adapters.ChamadoAdapter;
import com.chamei.pim4.util.Ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChamadosActivity extends AppCompatActivity implements ChamadoAdapter.Listener {

    private ActivityChamadosBinding binding;
    private ChamadosApi chamadosApi;
    private SessionManager session;
    private ChamadoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChamadosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        chamadosApi = ApiClient.get(this).create(ChamadosApi.class);

        adapter = new ChamadoAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.btnAdd.setOnClickListener(v -> showCreateDialog());
        binding.swipeRefresh.setOnRefreshListener(this::loadChamados);
        binding.btnBack.setOnClickListener(v -> finish());

        loadChamados();
    }

    private void setLoading(boolean loading) {
        binding.swipeRefresh.setRefreshing(loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadChamados() {
        setLoading(true);
        boolean isUsuario = "usuario".equalsIgnoreCase(session.getUserRole());
        Call<List<Chamado>> call;
        if (isUsuario) {
            Map<String, Integer> body = new HashMap<>();
            body.put("userId", session.getUserId());
            call = chamadosApi.listarPorUsuario(body);
        } else {
            call = chamadosApi.listar();
        }
        call.enqueue(new Callback<List<Chamado>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chamado>> call, @NonNull Response<List<Chamado>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Ui.toast(ChamadosActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao carregar"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chamado>> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(ChamadosActivity.this, "Falha: " + t.getMessage());
            }
        });
    }

    private void showCreateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        EditText titulo = new EditText(this);
        titulo.setHint("Título");
        layout.addView(titulo);

        EditText motivo = new EditText(this);
        motivo.setHint("Motivo");
        layout.addView(motivo);

        EditText desc = new EditText(this);
        desc.setHint("Descrição (opcional)");
        layout.addView(desc);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Novo chamado")
                .setView(layout)
                .setPositiveButton("Criar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = titulo.getText().toString().trim();
            String m = motivo.getText().toString().trim();
            String de = desc.getText().toString().trim();
            if (t.isEmpty() || m.isEmpty()) {
                Ui.toast(this, "Informe título e motivo");
                return;
            }
            dialog.dismiss();
            criarChamado(new CriarChamadoRequest(t, m, de, null));
        }));
        dialog.show();
    }

    private void criarChamado(CriarChamadoRequest req) {
        binding.progress.setVisibility(View.VISIBLE);
        chamadosApi.criar(req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                binding.progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Ui.toast(ChamadosActivity.this, "Chamado criado");
                    loadChamados();
                } else {
                    Ui.toast(ChamadosActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao criar chamado"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                binding.progress.setVisibility(View.GONE);
                Ui.toast(ChamadosActivity.this, "Falha: " + t.getMessage());
            }
        });
    }

    @Override
    public void onClick(Chamado chamado) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("chamadoId", chamado.id);
        i.putExtra("titulo", chamado.titulo);
        startActivity(i);
    }

    @Override
    public void onToggleStatus(Chamado chamado) {
        ProgressBar spinner = binding.progress;
        spinner.setVisibility(View.VISIBLE);
        Call<Map<String, Object>> call = chamado.resolvido
                ? chamadosApi.reabrir(chamado.id)
                : chamadosApi.fechar(chamado.id);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                spinner.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Ui.toast(ChamadosActivity.this, "Status atualizado");
                    loadChamados();
                } else {
                    Ui.toast(ChamadosActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao atualizar"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                spinner.setVisibility(View.GONE);
                Ui.toast(ChamadosActivity.this, "Falha: " + t.getMessage());
            }
        });
    }
}
