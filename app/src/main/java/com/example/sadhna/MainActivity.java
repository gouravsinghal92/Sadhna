package com.example.sadhna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etMobile, etFacilitator;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etFacilitator = findViewById(R.id.etFacilitator);

        // Configure Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("816325573985-prrn37fu4fdpcnfjplf76s566q4vg7re.apps.googleusercontent.com")
                .requestEmail()
                .build();

        //816325573985-ijtfhv96scilvrebpo8gd4frd9lpf0ad.apps.googleusercontent.com
        //816325573985-prrn37fu4fdpcnfjplf76s566q4vg7re.apps.googleusercontent.com

        // Initialize sign in client
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);

        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        // Initialize firebase user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        Button btnSubmit = findViewById(R.id.btnSubmit);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName =  sharedPreferences.getString("Name", "").toString();
        String mobileNumber = sharedPreferences.getString("Mobile", "").toString();
        String facilitator = sharedPreferences.getString("Facilitator", "").toString();

        if(firebaseUser != null && userName != "" && mobileNumber != "" && facilitator != "") {

            displayToast("Hari Bol! Authentication successful");

            // Proceed to Screen 2 or perform other actions
            Intent goToDataFormScreenIntent = new Intent(getApplicationContext(), SadhnaFormActivity.class);
            startActivity(goToDataFormScreenIntent);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String name = etName.getText().toString();
                String mobile = etMobile.getText().toString();
                String facilitator = etFacilitator.getText().toString();

                // Save data locally (e.g., using SharedPreferences)
                SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Name", name);
                editor.putString("Mobile", mobile);
                editor.putString("Facilitator", facilitator);
                editor.apply();

                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                String idToken = googleSignInAccount.getIdToken(); // This is your OAuth ID token
                // You can send this ID token to your server for further processing

                if (googleSignInAccount != null) {
                    // When sign in account is not equal to null initialize auth credential
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                    // Check credential
                    firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Check condition
                            if (task.isSuccessful()) {
                                // When task is successful redirect to profile activity display Toast
                                displayToast("Hari Bol! Authentication successful");

                                // Get the currently signed-in Firebase user
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                if (firebaseUser != null) {
                                    // Retrieve the ID token
                                    firebaseUser.getIdToken(true)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    // The ID token is obtained successfully
                                                    String idToken = task1.getResult().getToken();
                                                    // You can use 'idToken' as your access token
                                                    // Now, you can pass it to your Google Sheets API calls or other services.

                                                    SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putString("token", idToken);
                                                    editor.apply();

                                                    // Proceed to Screen 2 or perform other actions
                                                    Intent goToDataFormScreenIntent = new Intent(getApplicationContext(), SadhnaFormActivity.class);
                                                    startActivity(goToDataFormScreenIntent);

                                                } else {
                                                    // Handle the error
                                                    Exception exception = task1.getException();
                                                    if (exception != null) {
                                                        // Handle the exception
                                                    }
                                                }
                                            });
                                }

                            } else {
                                // When task is unsuccessful display Toast
                                displayToast("Authentication Failed :" + task.getException().getMessage());
                            }
                        }
                    });
                }

                Log.d("OAuth", "ID Token: " + idToken);

            } catch (ApiException e) {
                Log.w("OAuth", "Google sign in failed", e);
            }
        }
    }
}