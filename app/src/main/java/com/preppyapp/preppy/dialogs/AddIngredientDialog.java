package com.preppyapp.preppy.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.models.Ingredient;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.Objects;

public class AddIngredientDialog extends AppCompatDialogFragment {

    private EditText editTextIngredientName;

    private CollectionReference ingredientRef;

    private String userEmail;

    private Boolean ingredientExists;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        userEmail = Objects.requireNonNull(account).getEmail();
        assert userEmail != null;

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        ingredientRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("ingredients");


        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_ingredients, null);

        editTextIngredientName = v.findViewById(R.id.dialog_ingredient_ingredientNameEditText);
        FloatingActionButton fabAdd = v.findViewById(R.id.dialog_ingredient_addIngredientButton);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newIngredient = editTextIngredientName.getText().toString().toLowerCase().trim();
                editTextIngredientName.getText().clear();
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
                                        Toast.makeText(requireActivity(), "Ingredient already exists", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if (!ingredientExists) {
                                    String ingredientId = ingredientRef.document().getId();
                                    long millis = System.currentTimeMillis();
                                    Date millisDate = new Date(millis);
                                    Ingredient ingredient = new Ingredient(newIngredient, "", ingredientId, false, userEmail, millisDate, "");
                                    ingredientRef.document(ingredientId).set(ingredient);
                                }


                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });


                }
            }
        });

        dialog.setView(v);

        Button doneButton = v.findViewById(R.id.dialog_ingredient_doneButton);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });

        return dialog.create();


    }

}
