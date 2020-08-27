package com.preppyapp.preppy.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.models.Ingredient;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class RecipeActivityIngredientsAdapter extends FirestoreRecyclerAdapter<Ingredient, RecipeActivityIngredientsAdapter.IngredientHolder> {

    private String recipeName;

    public RecipeActivityIngredientsAdapter(@NonNull FirestoreRecyclerOptions<Ingredient> options, String recipeName) {
        super(options);
        this.recipeName = recipeName;
    }

    @Override
    protected void onBindViewHolder(@NonNull IngredientHolder holder, final int position, @NonNull Ingredient model) {
        holder.ingredientTextView.setText(model.getName());

        ArrayList<String> recipes = model.getRecipe();
        int index = recipes.indexOf(recipeName);
        ArrayList<String> amount = model.getAmount();


        if (amount.size() <= index) {
            int counter = index - amount.size() + 1;
            for (int i = 0; i < counter; i++) {
                amount.add("");
                getSnapshots().getSnapshot(position)
                        .getReference()
                        .update("amount", amount);
            }
        } else {
            holder.ingredientRecipeTextView.setText(amount.get(index));
        }


        if (model.isOut()) {
            holder.shoppingCartImg.setVisibility(View.VISIBLE);
        } else {
            holder.shoppingCartImg.setVisibility(View.INVISIBLE);
        }

    }

    @NonNull
    @Override
    public IngredientHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.recipe_ingredient_row, parent, false);
        return new IngredientHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    public void deleteItem(int position) {
        Ingredient ingredient = getSnapshots().getSnapshot(position).toObject(Ingredient.class);
        DocumentReference ingredientRef = getSnapshots().getSnapshot(position).getReference();
        assert ingredient != null;
        ArrayList<String> recipe = ingredient.getRecipe();
        ArrayList<String> amount = ingredient.getAmount();

        if (recipe.size() > 1) {
            int index = recipe.indexOf(recipeName);
            recipe.remove(recipeName);
            amount.remove(index);
            ingredientRef.update("recipe", recipe);
            ingredientRef.update("amount", amount);
        } else {
            ingredientRef.delete();
        }
        notifyDataSetChanged();
    }

    public void updateIsOut(int position) {
        Ingredient ingredient = getSnapshots().getSnapshot(position).toObject(Ingredient.class);

        DocumentReference ingredientRef = getSnapshots().getSnapshot(position).getReference();
        assert ingredient != null;
        ingredientRef.update("out", !ingredient.isOut());
        notifyDataSetChanged();
    }

    public static class IngredientHolder extends RecyclerView.ViewHolder {

        public TextView ingredientTextView;
        public TextView ingredientRecipeTextView;
        public ImageView shoppingCartImg;

        public IngredientHolder(@NonNull View itemView) {
            super(itemView);

            ingredientTextView = itemView.findViewById(R.id.recipeIngredient_list_name);
            ingredientRecipeTextView = itemView.findViewById(R.id.recipeIngredient_list_recipeName);
            shoppingCartImg = itemView.findViewById(R.id.recipeIngredient_list_shoppingCartImg);
        }
    }
}
