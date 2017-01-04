package natalio.com.bloodbank.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import natalio.com.bloodbank.R;

public class ListUsersActivity extends AppCompatActivity {

    private static final String TAG = "user";
    private DatabaseReference mDatabase;
    static Map<String, String> users = new HashMap<>();
    public static final String USER = "USER";
    public static List<String> userNames = new ArrayList<>();
    public static List<String> ids = new ArrayList<>();
    private EditText etSearch;
    ArrayAdapter<String> adapter;
    private static String currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        final ListView lv = (ListView) findViewById(R.id.listView);
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // make first character uppercase to match bloodtype in database


        final FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {

                users.clear();
                userNames.clear();
                lv.setAdapter(null);

                if (charSequence.length() > 0) {
                    mDatabase.orderByChild("bloodType"). startAt(charSequence.toString()).
                            addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    users.clear();
                                    userNames.clear();
                                    User currentUser = dataSnapshot.child(current.getUid()).getValue(User.class);
                                    currentId = current.getUid();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User user = snapshot.getValue(User.class);
                                        users.put(snapshot.getKey(), user.getUsername());
                                    }
                                    users.remove(currentId); //do not show current user in search filter

                                    userNames = new ArrayList<String>(users.values());
                                    ids = new ArrayList<String>(users.keySet());

                                    adapter = new ArrayAdapter<>(
                                            ListUsersActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            userNames);

                                    lv.setAdapter(adapter);
                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            Intent intent = new Intent(ListUsersActivity.this, DetailActivity.class);
                                            String id = ids.get(i);
                                            intent.putExtra(USER, id);
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

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        /*mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
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
        });*/

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
