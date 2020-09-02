package com.preppyapp.preppy.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.models.Recipe;
import com.preppyapp.preppy.models.Tags;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class AddRecipeDialog extends AppCompatDialogFragment {

    private ArrayList<String> tags = new ArrayList<>();

    private ArrayList<String> recipeList = new ArrayList<>();


    private EditText editTextRecipeName;


    private CollectionReference recipeRef;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_recipe, null);
        dialog.setView(v);


        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

        assert account != null;
        final String userEmail = account.getEmail();


        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        assert userEmail != null;
        DocumentReference tagRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("tags")
                .document("recipeTags");

        recipeRef = rootRef.collection("users")
                .document(userEmail)
                .collection("recipes");


        recipeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Recipe recipe = document.toObject(Recipe.class);
                        assert recipe != null;
                        recipeList.add(recipe.getRecipeName());
                    }
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });

        //Spinner reference
        final Spinner spinner = v.findViewById(R.id.dialog_recipe_recipeTagSpinner);
        spinner.bringToFront();


        tagRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tags.add(0, "no tag");
                if (documentSnapshot.exists()) {
                    Tags retrievedTags = documentSnapshot.toObject(Tags.class);
                    assert retrievedTags != null;
                    tags.addAll(retrievedTags.getTags());
                }


                //ArrayAdapter to create the spinner adapter
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        tags);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
            }
        });

        editTextRecipeName = v.findViewById(R.id.dialog_recipe_recipeNameEditText);


        //Add recipe
        Button addButton = v.findViewById(R.id.dialog_recipe_addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipeName = editTextRecipeName.getText().toString().trim().toLowerCase();
                String recipeTag = "";
                if (!(spinner.getSelectedItem() == null)) {
                    recipeTag = spinner.getSelectedItem().toString();
                }


                if (!(recipeName.equals(""))) {

                    if (recipeList.contains(recipeName)) {
                        Toast.makeText(requireContext(), "A recipe with this name already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        String recipeId = recipeRef
                                .document()
                                .getId();

                        Date date = new Date();

                        Recipe recipe = new Recipe(recipeName, recipeId, recipeTag, userEmail, date, "");

                        recipeRef.document(recipeId).set(recipe);

                        Objects.requireNonNull(getDialog()).dismiss();
                    }

                } else {
                    Toast.makeText(requireContext(), "Add a name for the recipe!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button cancelButton = v.findViewById(R.id.dialog_recipe_cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });

        return dialog.show();


    }


}
