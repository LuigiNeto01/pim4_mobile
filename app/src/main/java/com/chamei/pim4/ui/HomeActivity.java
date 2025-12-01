package com.chamei.pim4.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chamei.pim4.R;
import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.core.SessionManager;
import com.chamei.pim4.databinding.ActivityHomeBinding;
import com.chamei.pim4.model.Chamado;
import com.chamei.pim4.model.User;
import com.chamei.pim4.network.ChamadosApi;
import com.chamei.pim4.network.UsersApi;
import com.chamei.pim4.util.Ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard principal: exibe resumo de chamados, alertas, ultimos itens e
 * estatisticas de usuarios (para admin).
 */
public class HomeActivity extends AppCompatActivity {

    // Binding do layout do dashboard
    private ActivityHomeBinding binding;
    // Controle de sessao/autenticacao
    private SessionManager session;
    // Clientes de API usados na tela
    private ChamadosApi chamadosApi;
    private UsersApi usersApi;
    // Contador para sincronizar finalizacao das chamadas paralelas
    private int pendingCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        if (!session.isAuthenticated()) {
            // Sem token: manda para login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configura toolbar e menu
        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationIcon(R.drawable.ic_menu);
        binding.topAppBar.setNavigationOnClickListener(this::showMenuFromIcon);
        binding.topAppBar.setOnMenuItemClickListener(this::onMenuClick);

        // Instancia clientes de API
        chamadosApi = ApiClient.get(this).create(ChamadosApi.class);
        usersApi = ApiClient.get(this).create(UsersApi.class);

        boolean isAdmin = isAdmin();
        // Mostra/oculta botoes e cards conforme permissao
        binding.btnGoUsers.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        binding.userCard.setVisibility(View.VISIBLE); // Distribuicao por prioridade deve aparecer para todos
        binding.userStatsCard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (!isAdmin) {
            if (binding.topAppBar.getMenu().findItem(R.id.action_users) != null) {
                binding.topAppBar.getMenu().findItem(R.id.action_users).setVisible(false);
            }
        }

        binding.btnGoChamados.setOnClickListener(v -> startActivity(new Intent(this, ChamadosActivity.class)));
        binding.btnGoUsers.setOnClickListener(v -> startActivity(new Intent(this, UsersActivity.class)));

        loadDashboard();
    }

    private void showMenuFromIcon(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.inflate(R.menu.menu_home);
        if (!isAdmin()) {
            menu.getMenu().findItem(R.id.action_users).setVisible(false);
        }
        menu.setOnMenuItemClickListener(this::onMenuClick);
        menu.show();
    }

    private boolean onMenuClick(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_dashboard) {
            Ui.toast(this, "Voce esta no dashboard");
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
                Ui.toast(this, "Apenas admin pode gerir usuarios");
            }
            return true;
        }
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            // Limpa sessao e fecha a pilha
            session.clear();
            Ui.toast(this, "Sessao encerrada");
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return false;
    }

    private boolean isAdmin() {
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        return role.contains("admin");
    }

    private boolean isUsuario() {
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        return role.contains("usuario");
    }

    private void setLoading(boolean loading) {
        // Alterna entre spinner central e conteudo
        binding.dashboardProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void loadDashboard() {
        // Dispara chamadas necessarias; admin precisa de duas (chamados e usuarios)
        boolean admin = isAdmin();
        pendingCalls = admin ? 2 : 1;
        setLoading(true);

        // Escolhe endpoint de chamados conforme role
        Call<List<Chamado>> chamadosCall;
        if (isUsuario()) {
            Map<String, Integer> body = new HashMap<>();
            body.put("userId", session.getUserId());
            chamadosCall = chamadosApi.listarPorUsuario(body);
        } else {
            chamadosCall = chamadosApi.listar();
        }

        // Chamada de chamados
        chamadosCall.enqueue(new Callback<List<Chamado>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chamado>> call, @NonNull Response<List<Chamado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderChamados(response.body());
                } else {
                    Ui.toast(HomeActivity.this, "Nao foi possivel carregar chamados");
                }
                finishCall();
            }

            @Override
            public void onFailure(@NonNull Call<List<Chamado>> call, @NonNull Throwable t) {
                Ui.toast(HomeActivity.this, "Erro ao carregar chamados: " + t.getMessage());
                finishCall();
            }
        });

        // Admin tambem carrega estatisticas de usuarios
        if (admin) {
            usersApi.list().enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        renderUsers(response.body());
                    } else {
                        Ui.toast(HomeActivity.this, "Nao foi possivel carregar usuarios");
                    }
                    finishCall();
                }

                @Override
                public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                    Ui.toast(HomeActivity.this, "Erro ao carregar usuarios: " + t.getMessage());
                    finishCall();
                }
            });
        }
    }

    private void finishCall() {
        // Controla quando ambas as chamadas terminam para esconder loader
        pendingCalls -= 1;
        if (pendingCalls <= 0) {
            setLoading(false);
        }
    }

    private void renderChamados(List<Chamado> chamados) {
        try {
            int open = 0;
            int closed = 0;
            int criticalOpen = 0;
            int critica = 0;
            int alta = 0;
            int media = 0;
            int baixa = 0;
            List<Chamado> openChamados = new ArrayList<>();

            // Conta status e prioridade de cada chamado
            for (Chamado c : chamados) {
                boolean aberto = !c.resolvido;
                if (aberto) {
                    open++;
                    openChamados.add(c);
                } else {
                    closed++;
                }
                int p = c.prioridade;
                if (p == 1) {
                    critica++;
                    if (aberto) criticalOpen++;
                } else if (p == 2) {
                    alta++;
                } else if (p == 3) {
                    media++;
                } else {
                    baixa++;
                }
            }

            // Atualiza cards de contadores
            binding.txtOpenCount.setText(String.valueOf(open));
            binding.txtClosedCount.setText(String.valueOf(closed));
            binding.txtCriticalCount.setText(String.valueOf(criticalOpen));

            int total = chamados.size();
            updateProgress(binding.progressCritica, total, critica);
            updateProgress(binding.progressAlta, total, alta);
            updateProgress(binding.progressMedia, total, media);
            updateProgress(binding.progressBaixa, total, baixa);

            long oldOpen = countOlderThan(openChamados, 7);
            updateAlerts(criticalOpen, oldOpen);
            renderRecent(openChamados);
        } catch (Exception e) {
            Ui.toast(this, "Erro ao processar dados de chamados");
        }
    }

    private void updateProgress(ProgressBar bar, int total, int value) {
        // Evita divisao por zero ajustando o maximo
        int max = Math.max(total, 1);
        bar.setMax(max);
        bar.setProgress(value);
    }

    private long countOlderThan(List<Chamado> chamados, int days) {
        // Conta chamados abertos com mais de X dias
        long now = System.currentTimeMillis();
        long threshold = now - (long) days * 24 * 60 * 60 * 1000;
        long count = 0;
        for (Chamado c : chamados) {
            Date d = c.dataCriacao;
            if (d != null && d.getTime() < threshold) count++;
        }
        return count;
    }

    private void updateAlerts(int criticalOpen, long oldOpen) {
        // Mostra alertas conforme situacao dos chamados
        if (criticalOpen > 0) {
            binding.alertCritical.setVisibility(View.VISIBLE);
            binding.txtCriticalLabel.setText("Ha " + criticalOpen + " chamado(s) critico(s) aberto(s).");
        } else {
            binding.alertCritical.setVisibility(View.GONE);
        }

        if (oldOpen > 0) {
            binding.alertOld.setVisibility(View.VISIBLE);
            binding.txtOldLabel.setText(oldOpen + " chamado(s) aberto(s) com mais de 7 dias.");
        } else {
            binding.alertOld.setVisibility(View.GONE);
        }
    }

    private void renderRecent(List<Chamado> chamados) {
        // Monta lista de ultimos chamados abertos
        binding.recentContainer.removeAllViews();
        if (chamados.isEmpty()) {
            addRecentText("Sem chamados recentes");
            return;
        }

        Collections.sort(chamados, new Comparator<Chamado>() {
            @Override
            public int compare(Chamado o1, Chamado o2) {
                Date d1 = o1.dataCriacao;
                Date d2 = o2.dataCriacao;
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1);
            }
        });

        int limit = Math.min(5, chamados.size());
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        for (int i = 0; i < limit; i++) {
            Chamado c = chamados.get(i);
            String title = "#" + c.id + " " + safe(c.titulo);
            String subtitle = safe(c.descricao);
            String date = c.dataCriacao != null ? fmt.format(c.dataCriacao) : "";
            addRecentItem(title, subtitle, date);
        }
    }

    private void addRecentText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.chamou_subtitle));
        binding.recentContainer.addView(tv);
    }

    private void addRecentItem(String title, String subtitle, String date) {
        // Cria bloco vertical com titulo, descricao e data
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(0, 0, 0, 12);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.chamou_title));
        tvTitle.setTextSize(16);
        tvTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText(subtitle);
        tvSubtitle.setTextColor(ContextCompat.getColor(this, R.color.chamou_subtitle));

        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setTextColor(ContextCompat.getColor(this, R.color.chamou_subtitle));
        tvDate.setTextSize(12);

        item.addView(tvTitle);
        item.addView(tvSubtitle);
        item.addView(tvDate);
        binding.recentContainer.addView(item);
    }

    private void renderUsers(List<User> users) {
        try {
            int total = users != null ? users.size() : 0;
            int admins = 0;
            int suporte = 0;
            if (users != null) {
                for (User u : users) {
                    String cargo = safe(u.cargo).toLowerCase(Locale.ROOT);
                    if (cargo.contains("admin")) admins++;
                    if (cargo.contains("suporte")) suporte++;
                }
            }

            binding.txtUsersTotal.setText(String.valueOf(total));
            binding.txtUsersAdmin.setText(String.valueOf(admins));
            binding.txtUsersSuporte.setText(String.valueOf(suporte));
        } catch (Exception e) {
            Ui.toast(this, "Erro ao processar dados de usuarios");
        }
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}
