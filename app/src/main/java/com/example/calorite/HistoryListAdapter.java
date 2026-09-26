package com.example.calorite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryListViewHolder> {

    private Context context;
    private List<HistorySummary> historyList;

    public HistoryListAdapter(Context context, List<HistorySummary> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_list, parent, false);
        return new HistoryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryListViewHolder holder, int position) {
        HistorySummary summary = historyList.get(position);

        holder.tvDate.setText(summary.getDate() + " >");
        holder.tvCal.setText("🔥 " + summary.getTotalCalories() + " kcal");

        int totalProtein = AppDatabase.getInstance(context).foodDao().getTotalProteinByDate(summary.getDate());
        holder.tvProtein.setText("🥩 " + totalProtein + "g Protein");

        // --- LOGIKA MENGISI PIRING ---
        Drawable drawableCal = ContextCompat.getDrawable(context, R.drawable.img_plate_cal);
        Drawable drawablePro = ContextCompat.getDrawable(context, R.drawable.img_plate_pro);

        if (drawableCal != null && drawablePro != null) {
            ClipDrawable clipCal = new ClipDrawable(drawableCal.mutate(), Gravity.LEFT, ClipDrawable.HORIZONTAL);
            ClipDrawable clipPro = new ClipDrawable(drawablePro.mutate(), Gravity.LEFT, ClipDrawable.HORIZONTAL);

            holder.ivProgCal.setImageDrawable(clipCal);
            holder.ivProgPro.setImageDrawable(clipPro);

            // Ambil target harian
            SharedPreferences prefs = context.getSharedPreferences("CaloriteSettings", Context.MODE_PRIVATE);
            int targetCal = prefs.getInt("TARGET_CALORIE", 2000);
            int targetPro = prefs.getInt("TARGET_PROTEIN", 100);
            if (targetCal == 0) targetCal = 1;
            if (targetPro == 0) targetPro = 1;

            int totalCal = Integer.parseInt(summary.getTotalCalories());
            int percentCal = (int) (((float) totalCal / targetCal) * 100);
            int percentPro = (int) (((float) totalProtein / targetPro) * 100);

            int levelCalClip = Math.min(percentCal, 100) * 100;
            int levelProClip = percentPro == 0 ? 0 : 2000 + (Math.min(percentPro, 100) * 40);

            clipCal.setLevel(levelCalClip);
            clipPro.setLevel(levelProClip);

            holder.ivProgCal.invalidate();
            holder.ivProgPro.invalidate();
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryDetailActivity.class);
            intent.putExtra("HISTORY_DATE", summary.getDate());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryListViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivProgCal, ivProgPro;
        TextView tvDate, tvCal, tvProtein;

        public HistoryListViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivHistoryListThumb);
            ivProgCal = itemView.findViewById(R.id.ivListProgressCalorie);
            ivProgPro = itemView.findViewById(R.id.ivListProgressProtein);
            tvDate = itemView.findViewById(R.id.tvHistoryListDate);
            tvCal = itemView.findViewById(R.id.tvHistoryListCal);
            tvProtein = itemView.findViewById(R.id.tvHistoryListProtein);
        }
    }
}