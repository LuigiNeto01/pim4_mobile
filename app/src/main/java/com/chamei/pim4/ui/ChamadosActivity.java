package com.chamei.pim4.ui;

import android.content.Intent;
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
import com.chamei.pim4.databinding.ActivityChamadosBinding;
import com.chamei.pim4.model.AiOpinionResponse;
import com.chamei.pim4.model.Chamado;
import com.chamei.pim4.model.CriarChamadoRequest;
import com.chamei.pim4.network.AiApi;
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

/**
 * Tela de listagem/gestao de chamados. Permite filtrar, criar, fechar/reabrir e
 * abrir o chat associado a cada chamado. Usa AI para confirmar abertura.
 */
public class ChamadosActivity extends AppCompatActivity implements ChamadoAdapter.Listener {

    private static final String[] MOTIVOS = new String[]{
            "Problemas com o mouse",
            "Problemas com som",
            "Problema com video",
            "Problemas com a internet",
            "Outros"
    };

    // Binding da tela de chamados
    private ActivityChamadosBinding binding;
    // Cliente da API de chamados
    private ChamadosApi chamadosApi;
    // Cliente da API de IA (confirmacao/sugestao)
    private AiApi aiApi;
    // Sessao atual (usado para saber role e dados do usuario)
    private SessionManager session;
    // Adapter da lista
    private ChamadoAdapter adapter;
    // Cache dos chamados carregados para aplicar filtros sem nova chamada
    private final List<Chamado> todosChamados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChamadosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        chamadosApi = ApiClient.get(this).create(ChamadosApi.class);
        aiApi = ApiClient.get(this).create(AiApi.class);

        // Configura toolbar e menu superior
        Toolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(this::showMenuFromIcon);
        if (!isAdmin()) {
            toolbar.getMenu().findItem(R.id.action_users).setVisible(false);
        }

        adapter = new ChamadoAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        setupFilters();
        binding.btnAdd.setOnClickListener(v -> showCreateDialog());
        binding.swipeRefresh.setOnRefreshListener(this::loadChamados);

        loadChamados();
    }

    private boolean onMenuItemClick(@NonNull MenuItem item) {
        // Navegacao padrao via toolbar
        int id = item.getItemId();
        if (id == R.id.action_dashboard) {
            startActivity(new Intent(this, HomeActivity.class));
            return true;
        }
        if (id == R.id.action_chamados) return true;
        if (id == R.id.action_users) {
            startActivity(new Intent(this, UsersActivity.class));
            return true;
        }
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            // Limpa sessao e volta para login
            session.clear();
            Ui.toast(this, "Sessao encerrada");
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return false;
    }

    private void showMenuFromIcon(View anchor) {
        // Menu de overflow acionado pelo icone de nav
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, anchor);
        menu.inflate(R.menu.menu_home);
        if (!isAdmin()) {
            menu.getMenu().findItem(R.id.action_users).setVisible(false);
        }
        menu.setOnMenuItemClickListener(this::onMenuItemClick);
        menu.show();
    }

    private boolean isAdmin() {
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        return role.contains("admin");
    }

    private void setLoading(boolean loading) {
        binding.swipeRefresh.setRefreshing(loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadChamados() {
        // Carrega chamados considerando a role do usuario
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
                    todosChamados.clear();
                    todosChamados.addAll(response.body());
                    aplicarFiltros();
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
        // Dialog simples para criar chamado manualmente
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        EditText titulo = new EditText(this);
        titulo.setHint("Titulo");
        layout.addView(titulo);

        Spinner motivos = new Spinner(this);
        ArrayAdapter<String> adapterMotivos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MOTIVOS);
        motivos.setAdapter(adapterMotivos);
        layout.addView(motivos);

        EditText desc = new EditText(this);
        desc.setHint("Descricao (obrigatoria em Outros)");
        desc.setVisibility(View.GONE);
        layout.addView(desc);

        Spinner prioridades = new Spinner(this);
        ArrayAdapter<String> adapterPrior = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Critica (1)", "Alta (2)", "Media (3)", "Baixa (4)"});
        prioridades.setAdapter(adapterPrior);
        prioridades.setVisibility(View.GONE);
        layout.addView(prioridades);

        motivos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean outros = MOTIVOS[position].equals("Outros");
                desc.setVisibility(outros ? View.VISIBLE : View.GONE);
                prioridades.setVisibility(outros ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Novo chamado")
                .setView(layout)
                .setPositiveButton("Criar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = titulo.getText().toString().trim();
            String m = (String) motivos.getSelectedItem();
            String de = desc.getText().toString().trim();
            boolean outros = "Outros".equalsIgnoreCase(m);
            if (t.isEmpty() || m == null || m.isEmpty()) {
                Ui.toast(this, "Informe titulo e motivo");
                return;
            }
            Integer prioridade = mapPrioridade(m);
            if (outros) {
                if (de.isEmpty()) {
                    Ui.toast(this, "Descreva o problema em \"Outros\"");
                    return;
                }
                prioridade = parsePrioridade(prioridades.getSelectedItemPosition());
            }
            if (prioridade == null) prioridade = 4;
            dialog.dismiss();
            solicitarConfirmacaoIA(t, m, outros ? de : null, prioridade);
        }));
        dialog.show();
    }

    private void solicitarConfirmacaoIA(String titulo, String motivo, String descricao, Integer prioridade) {
        // Monta payload para a IA validar/explicar o chamado
        Map<String, Object> body = new HashMap<>();
        body.put("titulo", titulo);
        body.put("motivo", motivo);
        body.put("descricao", descricao != null ? descricao : "");
        body.put("prioridade", prioridade);
        body.put("nome", session.getUserName());
        body.put("email", session.getUserEmail());

        setLoading(true);
        aiApi.opiniaoChamado(body).enqueue(new Callback<AiOpinionResponse>() {
            @Override
            public void onResponse(@NonNull Call<AiOpinionResponse> call, @NonNull Response<AiOpinionResponse> response) {
                setLoading(false);
                String texto = response.body() != null ? response.body().text : null;
                if (texto == null || texto.isEmpty()) {
                    Ui.toast(ChamadosActivity.this, "Nao foi possivel obter confirmacao");
                    return;
                }
                mostrarDialogoConfirmacao(texto, titulo, motivo, descricao, prioridade);
            }

            @Override
            public void onFailure(@NonNull Call<AiOpinionResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(ChamadosActivity.this, "Falha ao obter confirmacao: " + t.getMessage());
            }
        });
    }

    private void mostrarDialogoConfirmacao(String texto, String titulo, String motivo, String descricao, Integer prioridade) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar envio")
                .setMessage(texto)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (d, w) -> criarChamado(new CriarChamadoRequest(titulo, motivo, descricao, prioridade)))
                .show();
    }

    private Integer mapPrioridade(String motivo) {
        // Prioridade pre-definida para motivos padrao
        String k = motivo == null ? "" : motivo.toLowerCase();
        if (k.equals("problemas com a internet")) return 1;
        if (k.equals("problema com video")) return 2;
        if (k.equals("problemas com o mouse")) return 3;
        if (k.equals("problemas com som")) return 4;
        return 4;
    }

    private Integer parsePrioridade(int idx) {
        switch (idx) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            default:
                return 4;
        }
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
        // Abre tela de chat passando dados principais
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("chamadoId", chamado.id);
        i.putExtra("titulo", chamado.titulo);
        i.putExtra("resolvido", chamado.resolvido);
        startActivity(i);
    }

    @Override
    public void onToggleStatus(Chamado chamado) {
        // Alterna entre fechar e reabrir chamado
        binding.progress.setVisibility(View.VISIBLE);
        Call<Map<String, Object>> call = chamado.resolvido
                ? chamadosApi.reabrir(chamado.id)
                : chamadosApi.fechar(chamado.id);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                binding.progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Ui.toast(ChamadosActivity.this, "Status atualizado");
                    loadChamados();
                } else {
                    Ui.toast(ChamadosActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao atualizar"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                binding.progress.setVisibility(View.GONE);
                Ui.toast(ChamadosActivity.this, "Falha: " + t.getMessage());
            }
        });
    }

    private void setupFilters() {
        // Configura spinners de status e prioridade
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Todos", "Abertos", "Fechados"});
        binding.spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> priorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Todas prioridades", "Critica (1)", "Alta (2)", "Media (3)", "Baixa (4)"});
        binding.spinnerPrioridade.setAdapter(priorAdapter);

        android.widget.AdapterView.OnItemSelectedListener listener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        };
        binding.spinnerStatus.setOnItemSelectedListener(listener);
        binding.spinnerPrioridade.setOnItemSelectedListener(listener);
    }

    private void aplicarFiltros() {
        // Aplica filtros selecionados na UI sobre a lista em memoria
        List<Chamado> filtrados = new ArrayList<>();
        String statusSel = (String) binding.spinnerStatus.getSelectedItem();
        String priorSel = (String) binding.spinnerPrioridade.getSelectedItem();
        for (Chamado c : todosChamados) {
            boolean statusOk = true;
            if ("Abertos".equals(statusSel)) statusOk = !c.resolvido;
            if ("Fechados".equals(statusSel)) statusOk = c.resolvido;

            boolean priorOk = true;
            if (priorSel != null) {
                if (priorSel.startsWith("Critica")) priorOk = c.prioridade == 1;
                else if (priorSel.startsWith("Alta")) priorOk = c.prioridade == 2;
                else if (priorSel.startsWith("Media")) priorOk = c.prioridade == 3;
                else if (priorSel.startsWith("Baixa")) priorOk = c.prioridade == 4;
            }
            if (statusOk && priorOk) filtrados.add(c);
        }
        adapter.setItems(filtrados);
    }
}
