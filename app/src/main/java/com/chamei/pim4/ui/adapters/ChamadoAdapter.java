package com.chamei.pim4.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chamei.pim4.databinding.ItemChamadoBinding;
import com.chamei.pim4.model.Chamado;
import com.chamei.pim4.R;

import java.util.ArrayList;
import java.util.List;

public class ChamadoAdapter extends RecyclerView.Adapter<ChamadoAdapter.ViewHolder> {

    public interface Listener {
        void onClick(Chamado chamado);
        void onToggleStatus(Chamado chamado);
    }

    private final List<Chamado> items = new ArrayList<>();
    private final Listener listener;

    public ChamadoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Chamado> novos) {
        items.clear();
        if (novos != null) items.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChamadoBinding binding = ItemChamadoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemChamadoBinding b;

        ViewHolder(ItemChamadoBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Chamado c) {
            b.txtTitulo.setText(c.titulo != null ? c.titulo : "Chamado");
            b.txtMotivo.setText(c.motivo != null ? c.motivo : "-");
            setPrioridade(b.txtPrioridade, c.prioridade);
            b.txtStatus.setText(c.resolvido ? "Fechado" : "Aberto");
            b.btnStatus.setText(c.resolvido ? "Reabrir" : "Fechar");
            b.txtCriador.setText(c.nomeCriador != null ? c.nomeCriador : "");

            b.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(c);
            });
            b.btnStatus.setOnClickListener(v -> {
                if (listener != null) listener.onToggleStatus(c);
            });
        }

        private void setPrioridade(TextView view, int prioridade) {
            String label;
            int color;
            switch (prioridade) {
                case 1:
                    label = "Crítica (1)";
                    color = view.getContext().getColor(R.color.priority_critica);
                    break;
                case 2:
                    label = "Alta (2)";
                    color = view.getContext().getColor(R.color.priority_alta);
                    break;
                case 3:
                    label = "Média (3)";
                    color = view.getContext().getColor(R.color.priority_media);
                    break;
                default:
                    label = "Baixa (4)";
                    color = view.getContext().getColor(R.color.priority_baixa);
            }
            view.setText(label);
            view.setTextColor(color);
        }
    }
}
