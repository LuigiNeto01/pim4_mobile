package com.chamei.pim4.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chamei.pim4.databinding.ItemChatMessageBinding;
import com.chamei.pim4.model.ChatMessageItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessageItem> items = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    public void setItems(List<ChatMessageItem> novos) {
        items.clear();
        if (novos != null) items.addAll(novos);
        notifyDataSetChanged();
    }

    public void add(ChatMessageItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding b;

        ViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(ChatMessageItem item) {
            b.txtNome.setText(item.nome != null ? item.nome : "Usuário");
            b.txtMensagem.setText(item.mensagem != null ? item.mensagem : "");
            Date dt = item.dataEnvio != null ? item.dataEnvio : new Date();
            b.txtData.setText(fmt.format(dt));
        }
    }
}
