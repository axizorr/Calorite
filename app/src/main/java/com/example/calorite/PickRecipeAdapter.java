package com.example.calorite;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PickRecipeAdapter extends RecyclerView.Adapter<PickRecipeAdapter.PickViewHolder> {

    private Context context;
    private List<RecipeRecord> recipeListOriginal;
    private List<RecipeRecord> recipeListFiltered;
    private int selectedPosition = -1; // Nilai awal -1 (belum ada yang dipilih)

    public PickRecipeAdapter(Context context, List<RecipeRecord> recipeList) {
        this.context = context;
        this.recipeListOriginal = new ArrayList<>(recipeList != null ? recipeList : new ArrayList<>());
        this.recipeListFiltered = new ArrayList<>(this.recipeListOriginal);
    }

    @NonNull
    @Override
    public PickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pick_recipe, parent, false);
        return new PickViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickViewHolder holder, int position) {
        RecipeRecord recipe = recipeListFiltered.get(position);

        if (holder.tvRecipeTitle != null) holder.tvRecipeTitle.setText(recipe.recipeName);
        if (holder.tvRecipeTitle != null) holder.tvRecipeTitle.setText(recipe.recipeName);
        if (holder.tvRecipeCal != null) holder.tvRecipeCal.setText(recipe.calories + " kcal 🔥");
        if (holder.tvRecipePro != null) holder.tvRecipePro.setText(recipe.protein + " gr 🥩");

        // --- MANAJEMEN SELEKSI ITEM (Dengan Proteksi Anti-Crash) ---
        if (selectedPosition == position) {
            try {
                holder.itemView.setBackgroundResource(R.drawable.bg_recipe_selected);
            } catch (Exception e) {
                holder.itemView.setBackgroundColor(Color.parseColor("#E2F7D4")); // Fallback jika drawable belum ada
            }
            if (holder.tvRecipePro != null) {
                holder.tvRecipePro.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.checkbox_on_background, 0);
            }
        } else {
            try {
                holder.itemView.setBackgroundResource(R.drawable.bg_recipe_unselected);
            } catch (Exception e) {
                holder.itemView.setBackgroundColor(Color.WHITE); // Fallback
            }
            if (holder.tvRecipePro != null) {
                holder.tvRecipePro.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }

        // --- AKSI KLIK ITEM ---
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            int currentPos = holder.getAdapterPosition();

            if (currentPos == RecyclerView.NO_POSITION) return;

            selectedPosition = currentPos;

            // PERBAIKAN UTAMA: Cegah notifyItemChanged(-1) yang bikin crash!
            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return recipeListFiltered.size();
    }

    public RecipeRecord getSelectedRecipe() {
        if (selectedPosition != -1 && selectedPosition < recipeListFiltered.size()) {
            return recipeListFiltered.get(selectedPosition);
        }
        return null;
    }

    public void filter(String text) {
        recipeListFiltered.clear();
        if (text == null || text.trim().isEmpty()) {
            recipeListFiltered.addAll(recipeListOriginal);
        } else {
            String query = text.toLowerCase().trim();
            for (RecipeRecord item : recipeListOriginal) {
                if (item.recipeName != null && item.recipeName.toLowerCase().contains(query)) {
                    recipeListFiltered.add(item);
                }
            }
        }
        selectedPosition = -1; // Reset seleksi saat pencarian
        notifyDataSetChanged();
    }

    public static class PickViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipeTitle, tvRecipeCal, tvRecipePro;

        public PickViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeTitle = itemView.findViewById(R.id.tvPickRecipeName);
            tvRecipeCal = itemView.findViewById(R.id.tvPickRecipeCalories);
            tvRecipePro = itemView.findViewById(R.id.tvPickRecipeProtein); // ID disesuaikan dengan milikmu
        }
    }
}