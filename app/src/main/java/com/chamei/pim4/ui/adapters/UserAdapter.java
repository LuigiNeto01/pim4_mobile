package com.chamei.pim4.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chamei.pim4.databinding.ItemUserBinding;
import com.chamei.pim4.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para a lista de usuarios na tela de administracao.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(User user);
        void onDelete(User user);
    }

    // Lista atual exibida
    private final List<User> items = new ArrayList<>();
    private final Listener listener;

    public UserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<User> novos) {
        // Substitui lista atual pela recebida da API
        items.clear();
        if (novos != null) items.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemUserBinding b;

        ViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(User u) {
            // Preenche textos basicos
            b.txtNome.setText(u.nome != null ? u.nome : "");
            b.txtEmail.setText(u.email != null ? u.email : "");
            b.txtCargo.setText(u.cargo != null ? u.cargo : "");
            b.txtNivel.setText(u.nivel != null ? "Nivel " + u.nivel : "-");

            // Aciona listeners do adapter
            b.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(u);
            });
            b.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(u);
            });
        }
    }
}
