package com.example.calorite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    // UBAH DI SINI: Gunakan RecipeRecord, bukan Recipe
    private List<RecipeRecord> recipeList;

    // UBAH DI SINI JUGA
    public RecipeAdapter(Context context, List<RecipeRecord> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // UBAH DI SINI JUGA
        RecipeRecord recipe = recipeList.get(position);

        holder.tvRecipeTitle.setText(recipe.recipeName);
        holder.tvRecipeCal.setText("🔥 " + recipe.calories + " kcal");
        holder.tvRecipePro.setText("🥩 " + recipe.protein + " gr");

        if (recipe.imageBase64 != null && !recipe.imageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(recipe.imageBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivRecipeImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.id); // Bawa "KTP" resepnya agar bisa dicari di database
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        TextView tvRecipeTitle, tvRecipeCal, tvRecipePro;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvRecipeCal = itemView.findViewById(R.id.tvRecipeCal);
            tvRecipePro = itemView.findViewById(R.id.tvRecipeProtein);
        }
    }
}