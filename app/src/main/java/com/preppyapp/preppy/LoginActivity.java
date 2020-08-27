package com.preppyapp.preppy;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.preppyapp.preppy.models.Tags;
import com.preppyapp.preppy.models.User;

import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "LoginActivity";

    private GoogleSignInClient googleSignInClient;


    private FirebaseAuth mAuth;
    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private DocumentReference tagRef;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();


    }

    private void signIn() {
        // Launches the sign in flow, the result is returned in onActivityResult
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount googleSignInAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userEmail = googleSignInAccount.getEmail();
                    String userName = googleSignInAccount.getDisplayName();
                    String tokenId = googleSignInAccount.getIdToken();


                    User user = new User(userEmail, userName, tokenId);

                    assert userEmail != null;
                    rootRef.collection("users").document(userEmail).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User successfully created;");
                        }
                    });


                    tagRef = rootRef
                            .collection("users")
                            .document(userEmail)
                            .collection("tags")
                            .document("recipeTags");


                    boolean newUser = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();

                    if (newUser) {
                        ArrayList<String> tags = new ArrayList<>();
                        tags.add("breakfast");
                        tags.add("lunch");
                        tags.add("dinner");
                        Tags tag = new Tags(tags, userEmail);
                        tagRef.set(tag);
                    }

                    FirebaseUser account = mAuth.getCurrentUser();
                    updateUI(account);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    updateUI(null);

                }

                // ...
            }
        });
    }


    public void updateUI(FirebaseUser account) {
        if (account != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        if (!checkWifiOnAndConnected()) {
            Toast.makeText(this, "Check wifi connection", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        assert wifiMgr != null;
        if (wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            return wifiInfo.getNetworkId() != -1;
        } else {
            return false;
        }
    }

}
