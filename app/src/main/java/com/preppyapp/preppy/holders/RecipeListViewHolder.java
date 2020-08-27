package com.preppyapp.preppy.holders;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.RecipeActivity;
import com.preppyapp.preppy.models.Recipe;

public class RecipeListViewHolder extends RecyclerView.ViewHolder {

    private TextView recipeNameTextView;

    public RecipeListViewHolder(View itemView) {
        super(itemView);
        this.recipeNameTextView = itemView.findViewById(R.id.recipe_list_name);
    }

    public void setRecipeList(final Recipe recipe) {
        String recipeName = recipe.getRecipeName();
        this.recipeNameTextView.setText(recipeName);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(v.getContext(), RecipeActivity.class);
                intent.putExtra("recipe", recipe);
                v.getContext().startActivity(intent);
            }
        });


    }
}
