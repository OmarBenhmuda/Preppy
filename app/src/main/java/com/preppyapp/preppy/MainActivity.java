package com.preppyapp.preppy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.preppyapp.preppy.dialogs.AddIngredientDialog;
import com.preppyapp.preppy.dialogs.AddRecipeDialog;
import com.preppyapp.preppy.models.User;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    private int fragController = 0;
    Fragment selectedFragment;

    private String userEmail;
    private boolean userExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        assert account != null;
        userEmail = account.getEmail();


        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference users = rootRef.collection("users");


        users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    userExists = false;
                    for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            assert user != null;
                            String currEmail = user.getUserEmail();
                            if (currEmail.equals(userEmail)) {
                                userExists = true;
                                return;
                            }
                        }
                    }
                    if (!userExists) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        mAuth.signOut();
                        finish();
                    }

                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });


        //Creating Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RecipesFragment()).commit();
        fragController = 0;


        final FloatingActionsMenu fabMenu = findViewById(R.id.fab_menu);

        FloatingActionButton addRecipeButton = findViewById(R.id.addRecipe);
        FloatingActionButton addIngredientButton = findViewById(R.id.addIngredient);


        fabMenu.bringToFront();


        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.collapse();
                openAddRecipeDialog();
            }
        });

        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.collapse();
                openAddIngredientsDialog();
            }
        });

        FrameLayout banner = findViewById(R.id.banner);

        TextView bannerTitle = findViewById(R.id.bannerTitle);

        banner.setBackgroundResource(R.color.gray);
        bannerTitle.setText(R.string.recipes);

    }

    public void openAddRecipeDialog() {
        AddRecipeDialog addRecipeDialog = new AddRecipeDialog();
        addRecipeDialog.show(getSupportFragmentManager(), "Add recipe dialog");
    }

    public void openAddIngredientsDialog() {
        AddIngredientDialog addIngredientDialog = new AddIngredientDialog();
        addIngredientDialog.show(getSupportFragmentManager(), "Add ingredient dialog");
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    FloatingActionsMenu fabMenu = findViewById(R.id.fab_menu);


                    FrameLayout banner = findViewById(R.id.banner);

                    TextView bannerTitle = findViewById(R.id.bannerTitle);

                    if (item.getItemId() == R.id.nav_meals) {
                        if (fragController != 0) {

                            fragController = 0;
                            selectedFragment = new RecipesFragment();

                            banner.setBackgroundResource(R.color.gray);
                            bannerTitle.setText(R.string.recipes);

                            fabMenu.bringToFront();
                            fabMenu.setVisibility(View.VISIBLE);
                            fabMenu.collapse();

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                        }
                    }

                    if (item.getItemId() == R.id.nav_ingredients) {
                        if (fragController != 1) {

                            fragController = 1;
                            selectedFragment = new IngredientsFragment();

                            banner.setBackgroundResource(R.color.gray);
                            bannerTitle.setText(R.string.ingredients);

                            fabMenu.bringToFront();
                            fabMenu.setVisibility(View.VISIBLE);
                            fabMenu.collapse();
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                        }
                    }


                    if (item.getItemId() == R.id.nav_settings) {
                        if (fragController != 2) {
                            fragController = 2;
                            selectedFragment = new SettingsFragment();

                            banner.setBackgroundResource(R.color.gray);
                            bannerTitle.setText(R.string.settings);

                            fabMenu.bringToFront();
                            fabMenu.collapse();
                            fabMenu.setVisibility(View.GONE);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                        }

                    }
                    return true;
                }
            };


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser account) {
        if (account == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


}
