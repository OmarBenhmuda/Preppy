package com.preppyapp.preppy.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.models.Recipe;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecipeAdapter extends FirestoreRecyclerAdapter<Recipe, RecipeAdapter.RecipeHolder> {

    public RecipeAdapter(@NonNull FirestoreRecyclerOptions<Recipe> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecipeHolder holder, int position, @NonNull Recipe model) {
        holder.recipeName.setText(model.getRecipeName());
    }

    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.recipe_row, parent, false);
        return new RecipeHolder(view);
    }

    public static class RecipeHolder extends RecyclerView.ViewHolder {


        public TextView recipeName;

        public RecipeHolder(@NonNull View itemView) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.recipe_list_name);
        }
    }
}
