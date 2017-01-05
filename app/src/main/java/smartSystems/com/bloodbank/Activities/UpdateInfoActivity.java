package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import smartSystems.com.bloodBank.R;
import smartSystems.com.bloodBank.Session.Session;

public class UpdateInfoActivity extends AppCompatActivity {

    private static final String TAG = "Donor";
    private static DatabaseReference mDatabase;
    private static FirebaseAuth mAuth;
    private Session session;
    private Button btnUpdate;
    private EditText etNewAddress, etNewPhone;
    private CheckBox newDonor;
    private static String newAddress, newPhone, newIsDonor;
    private static boolean isDonor = false;
    private static FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        session = new Session(this);
        if (!session.loggedIn()) {
            logout();
        }
        user = mAuth.getInstance().getCurrentUser();

        etNewAddress = (EditText) findViewById(R.id.etNewAddress);
        etNewPhone = (EditText) findViewById(R.id.etNewPhone);
        newDonor = (CheckBox) findViewById(R.id.newDonor);
        btnUpdate = (Button) findViewById(R.id.btnUpdateInfo);

        mDatabase.child("users").child(user.getUid()).child("donor").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newIsDonor = (String) dataSnapshot.getValue();
                        if (newIsDonor.equals("Yes")) {
                            isDonor = true;
                            newDonor.setText(R.string.DonorStatus);
                        } else {
                            isDonor = false;
                            newDonor.setText(R.string.NonDonorStatus);
                        }

                        Log.i(TAG, String.valueOf(isDonor));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newAddress = etNewAddress.getText().toString();
                newPhone = etNewPhone.getText().toString();

                if (!newAddress.isEmpty())
                    update("address", newAddress);

                if (!newPhone.isEmpty())
                    update("phone", newPhone);

                if (newDonor.isChecked()) {
                    if (isDonor)
                        update("donor", "No");
                    else if (!isDonor)
                        update("donor", "Yes");
                }

                if (newAddress.isEmpty() && newPhone.isEmpty() && !newDonor.isChecked())
                    Toast.makeText(UpdateInfoActivity.this, R.string.back_update, Toast.LENGTH_SHORT).show();

                if (!newAddress.isEmpty() || !newPhone.isEmpty() || newDonor.isChecked()) {
                    startActivity(new Intent(UpdateInfoActivity.this, UserActivity.class));
                    finish();
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        mDatabase.child("users").child(user.getUid()).child("donor").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newIsDonor = (String) dataSnapshot.getValue();
                        if (newIsDonor.equals("Yes")) {
                            isDonor = true;
                            newDonor.setText(R.string.DonorStatus);
                        } else {
                            newDonor.setText(R.string.NonDonorStatus);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static void update(String child, String value) {
        mDatabase.child("users").child(user.getUid()).child(child).setValue(value);
    }

    private void logout() {
        session.setLoggedIn(false);
        finish();
        startActivity(new Intent(UpdateInfoActivity.this, LoginActivity.class));
    }
}
