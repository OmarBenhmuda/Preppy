package com.preppyapp.preppy.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.preppyapp.preppy.R;
import com.preppyapp.preppy.holders.RecyclerViewAdapter;
import com.preppyapp.preppy.models.Tags;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AddTagDialog extends AppCompatDialogFragment {

    private ArrayList<String> tags = new ArrayList<>();


    RecyclerViewAdapter adapter;

    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
    private RecyclerView recyclerView;


    private EditText editTextTagName;
    private DocumentReference tagRef;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

        assert account != null;
        final String userEmail = account.getEmail();


        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        assert userEmail != null;
        tagRef = rootRef
                .collection("users")
                .document(userEmail)
                .collection("tags")
                .document("recipeTags");


        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_tag, null);

        dialog.setView(v);

        editTextTagName = v.findViewById(R.id.dialog_tag_tagEditText);


        //Save Tag
        Button savedButton = v.findViewById(R.id.dialog_tag_saveButton);
        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tags tag = new Tags(tags, userEmail);
                tagRef.set(tag);
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });

        Button cancelButton = v.findViewById(R.id.dialog_tag_cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });


        recyclerView = v.findViewById(R.id.dialog_tag_recyclerView);
        recyclerView.setLayoutManager(layoutManager);


        tagRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Tags retrievedTags = documentSnapshot.toObject(Tags.class);

                if (retrievedTags != null) {
                    tags = retrievedTags.getTags();
                }

                buildRecyclerView();


            }
        });

        FloatingActionButton addTagButton = v.findViewById(R.id.dialog_tag_addTagFAB);
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = editTextTagName.getText().toString().trim().toLowerCase();

                if (!(tag.equals("")) && !(tags.contains(tag))) {
                    tags.add(tag);


                    buildRecyclerView();

                }
                editTextTagName.getText().clear();

            }
        });
        return dialog.show();
    }

    public void buildRecyclerView(){
        adapter = new RecyclerViewAdapter(tags);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                removeItem(position);
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


        adapter.notifyDataSetChanged();
    }


    public void removeItem(int position) {
        tags.remove(position);
        adapter.notifyItemRemoved(position);
    }


}
