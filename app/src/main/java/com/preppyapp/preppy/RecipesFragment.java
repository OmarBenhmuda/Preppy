package com.preppyapp.preppy;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.preppyapp.preppy.dialogs.AddTagDialog;
import com.preppyapp.preppy.holders.RecipeListViewHolder;
import com.preppyapp.preppy.models.Recipe;
import com.preppyapp.preppy.models.Tags;

import java.util.ArrayList;


public class RecipesFragment extends Fragment {
    ArrayAdapter<String> arrayAdapter;


    private RecyclerView recyclerView;
    private TextView emptyView;

    private Spinner spinner;

    private FirestoreRecyclerAdapter<Recipe, RecipeListViewHolder> adapter;
    private FirestoreRecyclerOptions<Recipe> fireStoreRecyclerOptions;

    private AddTagDialog addTagDialog;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference recipeListRef;
    DocumentReference tagRef;

    private ArrayList<String> tags = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_recipes, container, false);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        assert account != null;
        String userEmail = account.getEmail();
        assert userEmail != null;
        recipeListRef = rootRef
                .collection("users").document(userEmail)
                .collection("recipes");
        tagRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("tags")
                .document("recipeTags");

        //Spinner reference
        spinner = v.findViewById(R.id.recipeTagSpinner);
        spinner.bringToFront();


        attachSpinner();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String item = parentView.getItemAtPosition(position).toString();
                if (item.equals("all recipes")) {
                    setRecipeAdapter();
                } else {
                    setRecipeAdapterByTag(item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        Button addTagButton = v.findViewById(R.id.addTagButton);
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddTagDialog();
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView = v.findViewById(R.id.recipes_recyclerView);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        emptyView = v.findViewById(R.id.recipes_empty);

        return v;
    }


    public void setRecipeAdapterByTag(String string) {
        Query query = recipeListRef.whereEqualTo("recipeTag", string).orderBy("recipeName", Query.Direction.ASCENDING);
        fireStoreRecyclerOptions = new FirestoreRecyclerOptions
                .Builder<Recipe>()
                .setQuery(query, Recipe.class)
                .build();

        attachRecyclerViewAdapter();
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }


    public void setRecipeAdapter() {
        Query query = recipeListRef.orderBy("recipeName", Query.Direction.ASCENDING);
        fireStoreRecyclerOptions = new FirestoreRecyclerOptions
                .Builder<Recipe>()
                .setQuery(query, Recipe.class)
                .build();

        attachRecyclerViewAdapter();
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public void attachRecyclerViewAdapter() {


        adapter = new FirestoreRecyclerAdapter<Recipe, RecipeListViewHolder>(fireStoreRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RecipeListViewHolder holder, int position, @NonNull Recipe model) {
                holder.setRecipeList(model);
            }

            @NonNull
            @Override
            public RecipeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_row, parent, false);
                return new RecipeListViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(R.string.add_some_recipes);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    emptyView.setText("");
                }
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

        };
        recyclerView.setAdapter(adapter);
    }

    public void openAddTagDialog() {
        addTagDialog = new AddTagDialog();
        onPause();
        addTagDialog.show(getParentFragmentManager(), "Add tag dialog");
    }


    public void attachSpinner() {
        tagRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Tags retrievedTags = documentSnapshot.toObject(Tags.class);
                    assert retrievedTags != null;
                    tags = retrievedTags.getTags();
                }
                tags.add(0, "all recipes");
                //ArrayAdapter to create the spinner adapter
                arrayAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        tags);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        attachSpinner();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
