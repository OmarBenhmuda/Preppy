package com.preppyapp.preppy.models;

import java.util.ArrayList;
import java.util.Date;

public class Ingredient {

    private String name, ingredientId;
    private ArrayList<String> recipe = new ArrayList<>();
    private ArrayList<String> amount = new ArrayList<>();
    private boolean out;
    private String uid;
    private Date creationDate;

    public Ingredient() {

    }


    public Ingredient(String name, String recipe, String ingredientId, boolean out, String createdBy, Date creationDate, String amount) {
        this.name = name;
        this.recipe.add(recipe);
        this.ingredientId = ingredientId;
        this.out = out;
        this.uid = createdBy;
        this.creationDate = creationDate;
        this.amount.add(amount);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getRecipe() {
        return recipe;
    }

    public boolean isOut() {
        return out;
    }

    public void setIsOut(boolean isOut) {
        this.out = isOut;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public String getUid() {
        return uid;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ArrayList<String> getAmount() {
        return amount;
    }

}
