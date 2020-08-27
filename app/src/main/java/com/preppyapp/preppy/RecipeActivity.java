package com.preppyapp.preppy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.preppyapp.preppy.dialogs.InstructionsDialog;
import com.preppyapp.preppy.holders.RecipeActivityIngredientsAdapter;
import com.preppyapp.preppy.models.Ingredient;
import com.preppyapp.preppy.models.Recipe;
import com.preppyapp.preppy.models.Tags;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecipeActivity extends AppCompatActivity {


    private ArrayList<String> tags = new ArrayList<>();

    private boolean ingredientExists;

    private String recipeName;
    private String recipeId;

    private int spinnerIndex;

    private String recipeTag;

    private Spinner spinner;

    private String userEmail;

    private RecipeActivityIngredientsAdapter adapter;

    private DocumentReference recipeRef;
    private CollectionReference ingredientRef;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        userEmail = Objects.requireNonNull(account).getEmail();
        assert userEmail != null;


        //Recipe variables
        final Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        assert recipe != null;
        recipeName = recipe.getRecipeName();
        recipeId = recipe.getRecipeId();


        //Fire-store references
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        recipeRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("recipes")
                .document(recipeId);
        ingredientRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("ingredients");
        DocumentReference tagRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("tags")
                .document("recipeTags");


        final EditText newIngredientEditText = findViewById(R.id.recipeActivity_addIngredient_editText);
        final EditText newIngredientAmountEditText = findViewById(R.id.recipeActivity_addIngredientAmount_editText);
        TextView recipeNameTextView = findViewById(R.id.recipe_banner_title);


        //Spinner reference
        spinner = findViewById(R.id.recipeActivity_recipeTagSpinner);
        spinner.bringToFront();
        tagRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Tags retrievedTags = documentSnapshot.toObject(Tags.class);
                    assert retrievedTags != null;
                    tags = retrievedTags.getTags();
                    recipeTag = recipe.getRecipeTag();

                    spinnerIndex = tags.indexOf(recipeTag);
                }


                //ArrayAdapter to create the spinner adapter
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        RecipeActivity.this,
                        android.R.layout.simple_spinner_item,
                        tags);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
                spinner.setSelection(spinnerIndex);
            }
        });


        setTitle(recipeName);
        recipeNameTextView.setText(recipeName);


        FloatingActionButton addIngredientButton = findViewById(R.id.recipeActivity_addIngredientFAB);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newIngredient = newIngredientEditText.getText().toString().toLowerCase().trim();
                final String newIngredientAmount = newIngredientAmountEditText.getText().toString().toLowerCase().trim();


                if (!newIngredient.equals("")) {

                    ingredientRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                ingredientExists = false;
                                for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    Ingredient ingredient = document.toObject(Ingredient.class);
                                    assert ingredient != null;
                                    if (newIngredient.equals(ingredient.getName())) {
                                        ingredientExists = true;
                                        if (!ingredient.getRecipe().contains(recipeName)) {
                                            updateRecipeAndAmount(ingredient.getIngredientId(), recipeName, newIngredientAmount);
                                            adapter.notifyDataSetChanged();
                                        }
                                        return;
                                    }
                                }
                                if (!ingredientExists) {
                                    String ingredientId = ingredientRef.document().getId();
                                    long millis = System.currentTimeMillis();
                                    Date millisDate = new Date(millis);
                                    Ingredient ingredient = new Ingredient(newIngredient, recipeName, ingredientId, false, userEmail, millisDate, newIngredientAmount);
                                    ingredientRef.document(ingredientId).set(ingredient);
                                    adapter.notifyDataSetChanged();
                                }


                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });


                }
                adapter.notifyDataSetChanged();
                newIngredientEditText.getText().clear();
                newIngredientAmountEditText.getText().clear();
            }
        });


        buildRecyclerView();

        FloatingActionButton instructionsButton = findViewById(R.id.recipeActivity_instructionsFAB);
        instructionsButton.bringToFront();
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstructionsDialog();
            }
        });

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
                finish();
            }
        });


        ImageView deleteRecipeButton = findViewById(R.id.recipeActivity_deleteRecipeButton);
        deleteRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder confirmDeleteDialog =
                        new AlertDialog.Builder(RecipeActivity.this);


                confirmDeleteDialog.setTitle("Confirm Delete...");
                confirmDeleteDialog.setMessage("Are you sure you want delete this recipe?");
                confirmDeleteDialog.setIcon(R.drawable.ic_baseline_delete_24);


                confirmDeleteDialog.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                                Query query = ingredientRef.whereArrayContains("recipe", recipeName);
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                                Ingredient ingredient = document.toObject(Ingredient.class);
                                                assert ingredient != null;
                                                ArrayList<String> recipe = ingredient.getRecipe();
                                                ArrayList<String> amount = ingredient.getAmount();
                                                if (recipe.size() > 1) {
                                                    int index = recipe.indexOf(recipeName);
                                                    recipe.remove(recipeName);
                                                    amount.remove(index);
                                                    ingredientRef.document(ingredient.getIngredientId()).update("recipe", recipe);
                                                    ingredientRef.document(ingredient.getIngredientId()).update("amount", amount);
                                                } else {
                                                    ingredientRef.document(ingredient.getIngredientId()).delete();
                                                }
                                            }
                                        } else {
                                            Log.d("TAG", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                                goBack();
                                finish();
                                recipeRef.delete();
                            }
                        });
                confirmDeleteDialog.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                confirmDeleteDialog.show();
            }
        });
    }

    private void goBack() {
        Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        if (!(spinner.getSelectedItem() == null)) {
            recipeRef.update("recipeTag", spinner.getSelectedItem().toString());
        }
    }

    public void buildRecyclerView() {
        Query query = ingredientRef.whereArrayContains("recipe", recipeName);
        FirestoreRecyclerOptions<Ingredient> options =
                new FirestoreRecyclerOptions.Builder<Ingredient>()
                        .setQuery(query, Ingredient.class)
                        .build();

        adapter = new RecipeActivityIngredientsAdapter(options, recipeName);

        RecyclerView recyclerView = findViewById(R.id.recipeActivity_ingredientsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.RIGHT) {
                    adapter.updateIsOut(position);
                    adapter.notifyItemChanged(position);
                } else {
                    adapter.deleteItem(position);
                    adapter.notifyItemRemoved(position);
                }
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.5f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return 1000f;
            }


            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(RecipeActivity.this, R.color.green))
                        .addSwipeRightLabel("Add to shopping list")
                        .setSwipeRightLabelColor(ContextCompat.getColor(RecipeActivity.this, R.color.white))
                        .addSwipeRightActionIcon(R.drawable.ic_ingredients_white)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(RecipeActivity.this, R.color.red))
                        .addSwipeLeftLabel("Delete")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(RecipeActivity.this, R.color.white))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_close_white)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public void updateRecipeAndAmount(String id, String recipeName, String amount) {
        DocumentReference ingredientRef = this.ingredientRef.document(id);
        ingredientRef.update("amount", FieldValue.arrayUnion(amount));
        ingredientRef.update("recipe", FieldValue.arrayUnion(recipeName));

    }

    public void openInstructionsDialog() {
        InstructionsDialog instructionsDialog = new InstructionsDialog(recipeId, recipeName);
        instructionsDialog.show(getSupportFragmentManager(), "instructions dialog");
    }


}