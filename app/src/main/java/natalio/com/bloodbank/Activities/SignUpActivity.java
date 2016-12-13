package natalio.com.bloodbank.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import natalio.com.bloodbank.Model.User;
import natalio.com.bloodbank.R;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "FB_SIGNUP";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText etPass;
    private EditText etEmail;
    private EditText etAddress;
    private EditText etPhone;
    private RadioGroup etRadioGroup;
    private RadioButton etGender;
    private Spinner etBloodType;
    private Button etCreate;
    final String[] bloodType = new String[1];
    private CheckBox etDonor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etEmail = (EditText) findViewById(R.id.User);
        etPass = (EditText) findViewById(R.id.Password);
        etAddress = (EditText) findViewById(R.id.Address);
        etPhone = (EditText) findViewById(R.id.Phone);
        etRadioGroup = (RadioGroup) findViewById(R.id.Gender);
        etCreate = (Button) findViewById(R.id.btnRegister);
        etBloodType = (Spinner) findViewById(R.id.BloodType);
        etDonor = (CheckBox) findViewById(R.id.Donor);

        etBloodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bloodType[0] = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        findViewById(R.id.btnRegister).setOnClickListener(this);


        // Get a reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                createUserAccount();
                break;
        }
    }

    private boolean checkFormFields() {
        String email, password, address, phone;
        email = etEmail.getText().toString();
        password = etPass.getText().toString();
        address = etAddress.getText().toString();
        phone = etPhone.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()) {
            etPass.setError("Password Required");
            return false;
        }
        if (address.isEmpty()) {
            etAddress.setError("Address required");
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone number required");
        }

        return true;
    }

    private void createUserAccount() {
        if (!checkFormFields())
            return;

        final String email = etEmail.getText().toString();
        final String password = etPass.getText().toString();
        final String address = etAddress.getText().toString();
        final String phone = etPhone.getText().toString();
        final int gender = etRadioGroup.getCheckedRadioButtonId();
        etGender = (RadioButton) findViewById(gender);
        final String donor;
        if (etDonor.isChecked()) {
            donor = "Yes";
        } else donor = "No";

        // Create the user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(SignUpActivity.this, "User created", Toast.LENGTH_SHORT)
                                    // .show();
                                    FirebaseUser FbUser = task.getResult().getUser();
                                    User user = new User(email, password, address, phone, (String) etGender.getText(), bloodType[0], donor);
                                    mDatabase.child("users").child(FbUser.getUid()).setValue(user);
                                    Toast.makeText(SignUpActivity.this, "Account created", Toast.LENGTH_SHORT);
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();

                                } else {
                                    Toast.makeText(SignUpActivity.this, "Account creation failed", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }/////
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, "This email address is already in use.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}
