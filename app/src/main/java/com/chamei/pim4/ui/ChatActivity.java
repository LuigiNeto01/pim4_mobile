package com.chamei.pim4.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chamei.pim4.core.ApiClient;
import com.chamei.pim4.databinding.ActivityChatBinding;
import com.chamei.pim4.model.ChatMessageItem;
import com.chamei.pim4.model.SendChatMessageRequest;
import com.chamei.pim4.network.ChatApi;
import com.chamei.pim4.ui.adapters.ChatAdapter;
import com.chamei.pim4.util.Ui;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tela de chat vinculada a um chamado especifico.
 * Carrega historico e permite enviar novas mensagens (se o chamado estiver aberto).
 */
public class ChatActivity extends AppCompatActivity {

    // Binding do layout do chat
    private ActivityChatBinding binding;
    // Cliente da API de chat
    private ChatApi chatApi;
    // Adapter que exibe mensagens
    private ChatAdapter adapter;
    // Identificador do chamado relacionado
    private int chamadoId;
    // Flag para saber se o chamado esta fechado
    private boolean chamadoFechado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Recupera dados enviados pela tela anterior
        chamadoId = getIntent().getIntExtra("chamadoId", 0);
        String titulo = getIntent().getStringExtra("titulo");
        chamadoFechado = getIntent().getBooleanExtra("resolvido", false);
        binding.txtTitulo.setText(titulo != null ? titulo : "Chat");

        chatApi = ApiClient.get(this).create(ChatApi.class);
        adapter = new ChatAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnBack.setOnClickListener(v -> finish());

        // Se chamado ja foi fechado, desabilita entrada de mensagens
        if (chamadoFechado) {
            binding.btnSend.setEnabled(false);
            binding.inputMessage.setEnabled(false);
            binding.inputMessage.setHint("Chamado fechado. Mensagens desabilitadas.");
        }

        loadMessages();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadMessages() {
        // Busca historico de mensagens do chamado
        setLoading(true);
        chatApi.listarMensagens(chamadoId).enqueue(new Callback<List<ChatMessageItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatMessageItem>> call, @NonNull Response<List<ChatMessageItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                    binding.recycler.scrollToPosition(Math.max(adapter.getItemCount() - 1, 0));
                } else {
                    Ui.toast(ChatActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao carregar chat"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatMessageItem>> call, @NonNull Throwable t) {
                setLoading(false);
                Ui.toast(ChatActivity.this, "Falha: " + t.getMessage());
            }
        });
    }

    private void sendMessage() {
        // Valida mensagem
        String msg = binding.inputMessage.getText() != null ? binding.inputMessage.getText().toString().trim() : "";
        if (msg.isEmpty()) {
            Ui.toast(this, "Digite uma mensagem");
            return;
        }
        binding.inputMessage.setText("");
        chatApi.enviar(chamadoId, new SendChatMessageRequest(msg)).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Response<java.util.Map<String, Object>> response) {
                if (!response.isSuccessful()) {
                    Ui.toast(ChatActivity.this, ApiClient.errorConverter().toMessage(response.errorBody(), "Erro ao enviar"));
                }
                // Recarrega lista para exibir mensagem recem enviada
                loadMessages();
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Throwable t) {
                Ui.toast(ChatActivity.this, "Falha: " + t.getMessage());
            }
        });
    }
}
