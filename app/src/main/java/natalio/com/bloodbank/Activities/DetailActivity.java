package natalio.com.bloodbank.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import natalio.com.bloodbank.R;

public class DetailActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private TextView userText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userText = (TextView) findViewById(R.id.userText);
        String userName= getIntent().getStringExtra(ListUsersActivity.USERNAME);
        userText.setText(userName);
    }
}
