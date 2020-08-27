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
import java.util.Objects;

public class IngredientsFragmentIngredientsAdapter extends FirestoreRecyclerAdapter<Ingredient, IngredientsFragmentIngredientsAdapter.IngredientHolder> {

    public IngredientsFragmentIngredientsAdapter(@NonNull FirestoreRecyclerOptions<Ingredient> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull IngredientHolder holder, final int position, @NonNull Ingredient model) {
        holder.ingredientTextView.setText(model.getName());
        ArrayList<String> arrayList = model.getRecipe();
        if (arrayList.size() > 1) {
            holder.ingredientRecipeTextView.setText(R.string.multiple_recipes);
        } else if (arrayList.size() == 1) {
            if (arrayList.get(0).equals("")) {
                holder.ingredientRecipeTextView.setText(R.string.miscellaneous);
            } else {
                holder.ingredientRecipeTextView.setText(arrayList.get(0));
            }

        } else {
            holder.ingredientRecipeTextView.setText("");
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

    public boolean deleteItem(int position) {

        int recipeCount = Objects.requireNonNull(getSnapshots().getSnapshot(position).toObject(Ingredient.class)).getRecipe().size();

        if (recipeCount > 1) {
            return false;
        } else {
            getSnapshots().getSnapshot(position).getReference().delete();
            notifyDataSetChanged();
            return true;

        }
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
