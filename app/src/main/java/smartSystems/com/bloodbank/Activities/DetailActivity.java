package smartSystems.com.bloodBank.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class DetailActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static String id;
    private static String userName;
    private static String bloodType;
    private static String phone;
    private static String address;
    private TextView tvUsername;
    private TextView tvBloodType;
    private TextView tvAddress;
    private TextView tvPhone;
    private Button btnSendEmail;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        id = getIntent().getStringExtra("id");

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvBloodType = (TextView) findViewById(R.id.tvBloodType);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        btnSendEmail = (Button) findViewById(R.id.btnSendEmail);

        mDatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userName = user.getUsername();
                bloodType = user.getBloodType();
                phone = user.getPhone();
                address = user.getAddress();

                tvBloodType.setText("You selected blood type: " + bloodType);
                tvUsername.setText(userName);
                tvAddress.setText(address);
                tvPhone.setText(phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] addresses = {userName};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Blood donation request");
                intent.putExtra(Intent.EXTRA_TEXT, "I saw your profile on the blood bank application");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        });
    }
}
