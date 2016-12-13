package natalio.com.bloodbank.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import natalio.com.bloodbank.Model.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import natalio.com.bloodbank.R;

public class ListUsersActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    public static List<User> users = new ArrayList<>();
    public static final String USERNAME = "USERNAME";
    public static List<String> userNames = new ArrayList<>();
    private EditText etSearch;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final ListView lv = (ListView) findViewById(R.id.listView);
        etSearch = (EditText) findViewById(R.id.etSearch);

        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            FirebaseUser current = mAuth.getInstance().getCurrentUser();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                users.clear();//reinitialize user when activity is invoked
                userNames.clear();
                User currentUser = dataSnapshot.child(current.getUid()).getValue(User.class);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                }
                users.remove(currentUser);

                for (User user : users) {
                    userNames.add(user.getUsername());
                }

                adapter = new ArrayAdapter<>(
                        ListUsersActivity.this,
                        android.R.layout.simple_list_item_1,
                        userNames);

                lv.setAdapter(adapter);

                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ListUsersActivity.this.adapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(ListUsersActivity.this, DetailActivity.class);
                        User selectedUser = users.get(i);
                        intent.putExtra(USERNAME, selectedUser.getUsername());
                        startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
