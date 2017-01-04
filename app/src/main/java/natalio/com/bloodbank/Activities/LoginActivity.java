package natalio.com.bloodbank.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import natalio.com.bloodbank.R;
import natalio.com.bloodbank.Session.Session;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ISLOGGED = "LOGGED";
    private final String TAG = "FB_SIGNIN";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etPass;
    private EditText etEmail;
    private Session session;
    private Button btnLogin, btnRegister, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        session = new Session(this);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnReset = (Button) findViewById(R.id.btnForgetPass);
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        Log.d("Session", String.valueOf(session.loggedIn()));
        if (session.loggedIn()) {
            startActivity(new Intent(LoginActivity.this, UserActivity.class));
            finish();
        }

        etEmail = (EditText) findViewById(R.id.etEmailAddr);
        etPass = (EditText) findViewById(R.id.etPassword);

        // Get a reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();
        // Attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "Signed in: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                }
            }
        };
    }

    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        // add the AuthListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove the AuthListener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                signUserIn();
                break;

            case R.id.btnRegister:
                //signUserOut();
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;

            case R.id.btnForgetPass:
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        }
    }

    private boolean checkFormFields() {
        String email, password;

        email = etEmail.getText().toString();
        password = etPass.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()) {
            etPass.setError("Password Required");
            return false;
        }

        return true;
    }

    private void signUserIn() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // sign the user in with email and password credentials
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    session.setLoggedIn(true);
                                    Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT)
                                            .show();
                                    startActivity(new Intent(LoginActivity.this, UserActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_SHORT)
                                            .show();
                                }


                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Invalid password.", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(LoginActivity.this, "No account with this email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUserOut() {
        // sign the user out
        mAuth.signOut();
        Toast.makeText(LoginActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

}
