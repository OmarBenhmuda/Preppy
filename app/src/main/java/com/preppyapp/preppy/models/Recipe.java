package com.preppyapp.preppy.models;

import java.io.Serializable;
import java.util.Date;

public class Recipe implements Serializable {
    private String recipeName, recipeId, recipeTag, uid, instructions;
    private Date creationDate;

    public Recipe() {
    }

    public Recipe(String name, String recipeId, String recipeTag, String createdBy, Date creationDate, String instructions) {
        this.recipeName = name;
        this.recipeId = recipeId;
        this.recipeTag = recipeTag;
        this.uid = createdBy;
        this.creationDate = creationDate;
        this.instructions = instructions;
    }

    public String getRecipeName() {
        return this.recipeName;
    }

    public String getRecipeId() {
        return this.recipeId;
    }

    public String getRecipeTag() {
        return this.recipeTag;
    }

    public String getUid() {
        return uid;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getInstructions() {
        return this.instructions;
    }
}
