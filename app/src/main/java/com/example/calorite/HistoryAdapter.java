package com.example.calorite;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistorySummary> historyList;

    public HistoryAdapter(Context context, List<HistorySummary> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Mengambil cetakan XML item_history_dashboard
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_dashboard, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistorySummary summary = historyList.get(position);

        // Memasukkan teks ke dalam kotak
        holder.tvHistoryDate.setText(summary.getDate());
        holder.tvHistoryCal.setText(summary.getTotalCalories() + " kcal");
        holder.tvHistoryPercent.setText(summary.getPercentage() + " Calorie reached");

        if (summary.getImageBase64() != null && !summary.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(summary.getImageBase64(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivHistoryThumb.setImageBitmap(decodedByte);
                holder.ivHistoryThumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // FUNGSI KLIK: Pindah ke halaman Detail Histori (Halaman yang ada Notes-nya)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryDetailActivity.class);
            // Kita bawa data tanggalnya agar halaman sebelah tahu hari apa yang diklik
            intent.putExtra("HISTORY_DATE", summary.getDate());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivHistoryThumb;
        TextView tvHistoryDate, tvHistoryCal, tvHistoryPercent;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHistoryThumb = itemView.findViewById(R.id.ivHistoryThumb);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistoryCal = itemView.findViewById(R.id.tvHistoryCal);
            tvHistoryPercent = itemView.findViewById(R.id.tvHistoryPercent);
        }
    }
}