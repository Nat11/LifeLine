package smartSystems.com.bloodBank.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class SearchResultActivity extends AppCompatActivity {

    private String searchedBloodType;
    private int searchedDistance;
    private DatabaseReference mDatabase;
    private static String donor, bloodType;
    private static String currentAddress;
    private static String address;
    private static LatLng currentLatLng;
    private static List<String> userNameBloodList = new ArrayList<>();
    private static List<User> usersList = new ArrayList<>();
    static Map<String, String> users = new HashMap<>();
    ArrayAdapter<String> adapter;
    private static List<LatLng> addresses = new ArrayList<>();
    private static List<String> ids = new ArrayList<>();
    private Button btnSwitchToMaps;
    Parcelable state;
    private static String userName;
    private static ListView lv;
    private static Map<String, String> keysUserNames = new HashMap<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        searchedBloodType = getIntent().getStringExtra("blood");
        searchedDistance = getIntent().getIntExtra("distance", 0);

        btnSwitchToMaps = (Button) findViewById(R.id.btnSwitchToMaps);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        lv = (ListView) findViewById(R.id.listViewResult);
        final FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        clearLists();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();
        mDatabase.child("users").child(current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clearLists();

                if (dataSnapshot.hasChildren()) {
                    Map<String, String> values = (Map<String, String>) dataSnapshot.getValue();
                    donor = values.get("donor");
                    currentAddress = values.get("address");//Get address of current user
                    currentLatLng = getLatLng(currentAddress); //Convert address to Latitude Longitude
                }

                if (donor.equals("Yes")) {
                    mDatabase.child("users").orderByChild("donor").equalTo("No").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                userName = user.getUsername();
                                address = user.getAddress();
                                bloodType = user.getBloodType();
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                if (bloodType.equals(searchedBloodType) && distance / 1000 < searchedDistance) { //divise by 1000 to get distance in Kilometers
                                    users.put(snapshot.getKey(), user.getBloodType() + " : " + user.getUsername()); //display searchedBloodType and username in ListView
                                    addresses.add(getLatLng(address));
                                    keysUserNames.put(snapshot.getKey(), userName);
                                    usersList.add(user);
                                }
                            }
                            if (users.size() == 0) {
                                Toast.makeText(SearchResultActivity.this, "Invalid search criteria, please change your choices", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SearchResultActivity.this, AdvancedSearchActivity.class));

                            } else {
                                userNameBloodList = new ArrayList<String>(users.values());

                                Collections.sort(userNameBloodList);//Sort userNameBloodList by values
                                adapter = new ArrayAdapter<>(
                                        SearchResultActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        userNameBloodList);

                                ids = new ArrayList<String>(users.keySet());
                                lv.setAdapter(adapter);
                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent intent = new Intent(SearchResultActivity.this, DetailActivity.class);
                                        String id = ids.get(i);
                                        intent.putExtra("id", id);
                                        startActivity(intent);
                                    }
                                });

                                btnSwitchToMaps.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        goToMaps(addresses, usersList, keysUserNames);
                                    }
                                });

                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if (donor.equals("No")) {
                    progressDialog.show();
                    mDatabase.child("users").orderByChild("donor").equalTo("Yes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                address = user.getAddress();
                                bloodType = user.getBloodType();
                                userName = user.getUsername();
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                if (bloodType.equals(searchedBloodType) && distance / 1000 < searchedDistance) { //divise by 1000 to get distance in Kilometers
                                    users.put(snapshot.getKey(), user.getUsername());
                                    keysUserNames.put(snapshot.getKey(), userName);
                                    addresses.add(getLatLng(address));
                                    usersList.add(user);
                                }
                            }
                            if (users.size() == 0) {
                                Toast.makeText(SearchResultActivity.this, "Invalid search criteria", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SearchResultActivity.this, AdvancedSearchActivity.class));

                            } else {
                                userNameBloodList = new ArrayList<String>(users.values());
                                Collections.sort(userNameBloodList);//Sort userNameBloodList by values
                                adapter = new ArrayAdapter<>(
                                        SearchResultActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        userNameBloodList);

                                ids = new ArrayList<String>(users.keySet());
                                lv.setAdapter(adapter);
                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent intent = new Intent(SearchResultActivity.this, DetailActivity.class);
                                        String id = ids.get(i);
                                        intent.putExtra("id", id);
                                        startActivity(intent);
                                    }
                                });
                                btnSwitchToMaps.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        goToMaps(addresses, usersList, keysUserNames);
                                    }
                                });
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("UserInfo", "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    protected void onPause() {
        state = lv.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    public LatLng getLatLng(String address) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String adresseencoder = URLEncoder.encode(address);
        String uri = "http://maps.google.com/maps/api/geocode/json?address="
                + adresseencoder + "&sensor=false";

        HttpGet httpGet = new HttpGet(uri);

        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();

            InputStream stream = entity.getContent();

            int byteData;
            while ((byteData = stream.read()) != -1) {
                stringBuilder.append((char) byteData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        double lat = 0.0, lng = 0.0;

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
            lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");
            lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new LatLng(lat, lng);
    }

    public void goToMaps(List<LatLng> addresses, List<User> usersList, Map<String, String> keysUserNames) {
        Intent intent = new Intent(SearchResultActivity.this, MapsActivity.class);
        intent.putExtra("ADDRESSES", (Serializable) addresses);
        intent.putExtra("KEYSUSERNAMES", (Serializable) keysUserNames);
        intent.putExtra("USERS", (Serializable) usersList);
        startActivity(intent);
    }

    public void clearLists() {
        users.clear();
        userNameBloodList.clear();
        addresses.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SearchResultActivity.this, AdvancedSearchActivity.class));
    }
}
