package com.preppyapp.preppy.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.models.Recipe;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class InstructionsDialog extends AppCompatDialogFragment {

    private String recipeId;
    private String recipeName;

    public InstructionsDialog(String recipeId, String recipeName) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
    }

    private DocumentReference recipeRef;

    private String instructions;

    private EditText editTextInstructions;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(recipeName.toUpperCase() + " INSTRUCTIONS");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_instructions, null);

        dialog.setView(v);

        editTextInstructions = v.findViewById(R.id.dialog_instructions_editText);


        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

        assert account != null;
        final String userEmail = account.getEmail();


        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        assert userEmail != null;
        recipeRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("recipes")
                .document(recipeId);

        getInstructions();


        Button doneButton = v.findViewById(R.id.dialog_instructions_doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInstructions();
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });


        return dialog.show();


    }

    @Override
    public void onStart() {
        super.onStart();
        getInstructions();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveInstructions();
    }

    public void getInstructions() {
        recipeRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Recipe recipe = documentSnapshot.toObject(Recipe.class);
                assert recipe != null;
                instructions = recipe.getInstructions();
                editTextInstructions.setText(instructions, TextView.BufferType.EDITABLE);
            }
        });
    }

    public void saveInstructions() {
        instructions = editTextInstructions.getText().toString();
        recipeRef.update("instructions", instructions);
    }
}
