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

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatApi chatApi;
    private ChatAdapter adapter;
    private int chamadoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chamadoId = getIntent().getIntExtra("chamadoId", 0);
        String titulo = getIntent().getStringExtra("titulo");
        binding.txtTitulo.setText(titulo != null ? titulo : "Chat");

        chatApi = ApiClient.get(this).create(ChatApi.class);
        adapter = new ChatAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnBack.setOnClickListener(v -> finish());

        loadMessages();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadMessages() {
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
                loadMessages();
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Throwable t) {
                Ui.toast(ChatActivity.this, "Falha: " + t.getMessage());
            }
        });
    }
}
