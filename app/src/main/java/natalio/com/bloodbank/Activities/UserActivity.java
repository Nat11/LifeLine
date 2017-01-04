package natalio.com.bloodbank.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Map;

import natalio.com.bloodbank.R;
import natalio.com.bloodbank.Session.Session;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference mDatabase;
    private Session session;
    private TextView etUser;
    private TextView NavUser;
    private TextView etAddr;
    private TextView etPhoneNum;
    private TextView etGender;
    private TextView etBloodType;
    private TextView etDonor;
    private Button btnListUsers;
    private Button btnUpdate;

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

        etUser = (TextView) findViewById(R.id.etUser);
        etAddr = (TextView) findViewById(R.id.etAddr);
        etPhoneNum = (TextView) findViewById(R.id.etPhoneNum);
        etGender = (TextView) findViewById(R.id.etGender);
        etBloodType = (TextView) findViewById(R.id.etBloodType);
        etDonor = (TextView) findViewById(R.id.etIsDonor);
        btnListUsers = (Button) findViewById(R.id.btnListUsers);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        Map<String, String> values = (Map<String, String>) dataSnapshot.getValue();
                        etUser.setText(values.get("username"));
                        NavUser.setText(values.get("username"));
                        etAddr.setText(values.get("address"));
                        etPhoneNum.setText(values.get("phone"));
                        etGender.setText(values.get("gender"));
                        etBloodType.setText(values.get("bloodType"));
                        etDonor.setText(values.get("donor"));

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("UserInfo", "getUser:onCancelled", databaseError.toException());

                }
            });
        }

        btnListUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, ListUsersActivity.class));
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, UpdateInfoActivity.class));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
