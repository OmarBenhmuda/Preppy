package com.preppyapp.preppy;


import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.preppyapp.preppy.holders.IngredientsFragmentIngredientsAdapter;
import com.preppyapp.preppy.models.Ingredient;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class IngredientsFragment extends Fragment {

    private RecyclerView recyclerView;

    private IngredientsFragmentIngredientsAdapter adapter;
    private CollectionReference ingredientRef;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_ingredients, container, false);


        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        assert account != null;
        String userEmail = account.getEmail();


        assert userEmail != null;
        ingredientRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("ingredients");


        Button clearShoppingListButton = v.findViewById(R.id.clear_shoppingListButton);
        clearShoppingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ingredientRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Ingredient ingredient = document.toObject(Ingredient.class);
                                assert ingredient != null;
                                ingredientRef.document(ingredient.getIngredientId()).update("out", false);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });

        CheckBox showShoppingListCheckbox = v.findViewById(R.id.showShoppingListCheckbox);
        showShoppingListCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buildRecyclerView(isChecked);
            }
        });

        recyclerView = v.findViewById(R.id.ingredients_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
        buildRecyclerView(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    public void buildRecyclerView(boolean check) {
        Query query;
        if (check) {
            query = ingredientRef.whereEqualTo("out", true);
        } else {
            query = ingredientRef;
        }

        FirestoreRecyclerOptions<Ingredient> options =
                new FirestoreRecyclerOptions.Builder<Ingredient>()
                        .setQuery(query, Ingredient.class)
                        .build();

        adapter = new IngredientsFragmentIngredientsAdapter(options);


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
                    if (!adapter.deleteItem(position)) {
                        adapter.notifyItemChanged(position);
                        Toast toast = Toast.makeText(requireContext(), "Ingredient belongs to \nmore than one recipe", Toast.LENGTH_SHORT);
                        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                        if (v != null) v.setGravity(Gravity.CENTER);
                        toast.show();
                    } else {
                        adapter.notifyItemRemoved(position);
                    }

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
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                        .addSwipeRightLabel("Add to shopping list")
                        .setSwipeRightLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
                        .addSwipeRightActionIcon(R.drawable.ic_ingredients_white)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        .addSwipeLeftLabel("Delete")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
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
}
