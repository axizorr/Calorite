package com.example.calorite; // Sesuaikan package-mu

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DailyFoodAdapter extends RecyclerView.Adapter<DailyFoodAdapter.DailyViewHolder> {

    private Context context;
    private List<FoodRecord> foodList;

    public DailyFoodAdapter(Context context, List<FoodRecord> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_food, parent, false);
        return new DailyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        FoodRecord food = foodList.get(position);

        holder.tvDailyCal.setText("🔥 " + food.calories + " kcal");
        holder.tvDailyProtein.setText("🥩 " + food.protein + "gr Protein");

        // MENGUBAH BASE64 JADI GAMBAR
        if (food.imageBase64 != null && !food.imageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(food.imageBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivDailyFood.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.btnDeleteDailyFood.setOnClickListener(v -> {
            // 1. Hapus dari Database
            AppDatabase.getInstance(context).foodDao().deleteFood(food);

            // 2. Hapus dari daftar di layar (efek animasi hapus yang mulus)
            foodList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, foodList.size());

            // 3. Beritahu Activity untuk menghitung ulang total kalori & cat piring atas
            if (context instanceof HistoryDetailActivity) {
                ((HistoryDetailActivity) context).refreshDailyData();
            }

            android.widget.Toast.makeText(context, "Makanan dihapus!", android.widget.Toast.LENGTH_SHORT).show();
        });

        // FUNGSI KLIK: Memanggil pop-up Fullscreen di HistoryDetailActivity
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof HistoryDetailActivity) {
                ((HistoryDetailActivity) context).showImagePreviewDialog(food.imageBase64);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class DailyViewHolder extends RecyclerView.ViewHolder {
        public Button btnDeleteDailyFood;
        ImageView ivDailyFood;
        TextView tvDailyProtein, tvDailyCal;

        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            btnDeleteDailyFood = itemView.findViewById(R.id.btnDeleteDailyFood);
            ivDailyFood = itemView.findViewById(R.id.ivDailyFood);
            tvDailyProtein = itemView.findViewById(R.id.tvDailyProtein);
            tvDailyCal = itemView.findViewById(R.id.tvDailyCal);
        }
    }
}