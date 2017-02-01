package smartSystems.com.bloodBank.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import smartSystems.com.bloodBank.R;
import smartSystems.com.bloodBank.Session.Session;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference mDatabase;
    private static FirebaseUser currentUser;
    private Session session;
    private TextView etUser;
    private TextView NavUser;
    private TextView etAddr;
    private TextView etPhoneNum;
    private TextView etGender;
    private TextView etBloodType;
    private TextView etDonor;
    private Button btnDefaultSearch;
    private ProgressDialog progressDialog;
    private static String username, address, phone, gender, bloodType, isDonor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setTitle("Profile"); //set title on action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //final View vi = inflater.inflate(R.layout.nav_header_user, null); //Get nav_header_user layout

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        NavUser = (TextView) header.findViewById(R.id.textUser);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        session = new Session(this);

        if (!session.loggedIn()) {
            logout();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        etUser = (TextView) findViewById(R.id.etUser);
        etAddr = (TextView) findViewById(R.id.etAddr);
        etPhoneNum = (TextView) findViewById(R.id.etPhoneNum);
        etGender = (TextView) findViewById(R.id.etGender);
        etBloodType = (TextView) findViewById(R.id.etBloodType);
        etDonor = (TextView) findViewById(R.id.etIsDonor);
        btnDefaultSearch = (Button) findViewById(R.id.btnDefaultSearch);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        loadData();

        btnDefaultSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, LoadScreenActivity.class));
            }
        });
    }

    public void loadData() {
        progressDialog.show();
        mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Map<String, String> values = (Map<String, String>) dataSnapshot.getValue();
                    username = values.get("username");
                    address = values.get("address");
                    phone = values.get("phone");
                    gender = values.get("gender");
                    bloodType = values.get("bloodType");
                    isDonor = values.get("donor");
                }
                NavUser.setText(username);
                etUser.setText(username);
                etAddr.setText(address);
                etGender.setText(gender);
                etPhoneNum.setText(phone);
                etBloodType.setText(bloodType);
                etDonor.setText(isDonor);
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("UserInfo", "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    private void logout() {
        session.setLoggedIn(false);
        finish();
        startActivity(new Intent(UserActivity.this, LoginActivity.class));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            moveTaskToBack(true);
            //startActivity(new Intent(UserActivity.this, MainActivity.class));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userName", etUser.getText().toString());
        outState.putString("userAddress", etAddr.getText().toString());
        outState.putString("userPhone", etPhoneNum.getText().toString());
        outState.putString("userGender", etGender.getText().toString());
        outState.putString("userBloodType", etBloodType.getText().toString());
        outState.putString("userDonor", etDonor.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        etUser.setText(savedInstanceState.getString("userName"));
        etAddr.setText(savedInstanceState.getString("userAddress"));
        etPhoneNum.setText(savedInstanceState.getString("userPhone"));
        etGender.setText(savedInstanceState.getString("userGender"));
        etBloodType.setText(savedInstanceState.getString("userBloodType"));
        etDonor.setText(savedInstanceState.getString("userDonor"));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_update) {
            startActivity(new Intent(UserActivity.this, UpdateInfoActivity.class));

        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
