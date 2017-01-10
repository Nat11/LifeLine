package smartSystems.com.bloodBank.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.MarkerManager;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class DefaultMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static List<LatLng> addresses = new ArrayList<>();
    private static LatLng currentLatLng;
    private DatabaseReference mDatabase;
    private static String donor;
    private static String currentAddress;
    private static String address;
    private static String userName;
    static Map<String, User> users = new HashMap<>();
    private static List<String> ids = new ArrayList<>();
    private static List<User> matchedUsers = new ArrayList<>();
    private static Map<String, String> userNames = new HashMap<>();
    private final double maxDistance = 150.0;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_default_maps);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DefaultMapsActivity.this, AdvancedSearchActivity.class));
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        addresses.clear();
        users.clear();
        userNames.clear();

        progressDialog.show();
        if (current != null) {
            mDatabase.child("users").child(current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
                                    double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                    if (distance / 1000 < maxDistance) { //divise by 1000 to get distance in Kilometers
                                        users.put(snapshot.getKey(), user);
                                        userNames.put(snapshot.getKey(), userName);
                                        addresses.add(getLatLng(address));
                                    }
                                }
                                matchedUsers = new ArrayList<User>(users.values());
                                ids = new ArrayList<String>(users.keySet());
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    if (donor.equals("No")) {
                        mDatabase.child("users").orderByChild("donor").equalTo("Yes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    User user = snapshot.getValue(User.class);
                                    address = user.getAddress();
                                    userName = user.getUsername();
                                    double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                    if (distance / 1000 < maxDistance) {//divise by 1000 to get distance in Kilometers
                                        users.put(snapshot.getKey(), user);
                                        userNames.put(snapshot.getKey(), userName);
                                        addresses.add(getLatLng(address));
                                    }
                                }
                                matchedUsers = new ArrayList<User>(users.values());
                                ids = new ArrayList<String>(users.keySet());
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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setUpMapIfNeeded();
                        }
                    });
                }
            }).start();

        } else {
            Toast.makeText(this, "Current user was not retrieved", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng Paris = new LatLng(48.866667, 2.333333);

        for (int i = 0; i < matchedUsers.size(); i++) {

            mMap.addMarker(new MarkerOptions().position(getLatLng(matchedUsers.get(i).getAddress())).
                    title(matchedUsers.get(i).getUsername()).
                    snippet(matchedUsers.get(i).getBloodType()));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Paris, 5));


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(DefaultMapsActivity.this, DetailActivity.class);
                String userName = marker.getTitle();
                String clickedMarkerUserId = getKeysFromValue(userNames, userName); //get User Id from database
                String result = clickedMarkerUserId.substring(1, clickedMarkerUserId.length() - 1);
                Log.d("clickedUser", result);
                intent.putExtra("id", result);
                startActivity(intent);
            }
        });
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

    private static String getKeysFromValue(Map hm, Object value) {
        String val = null;
        Set ref = hm.keySet();
        Iterator it = ref.iterator();
        List list = new ArrayList();

        while (it.hasNext()) {
            Object o = it.next();
            if (hm.get(o).equals(value)) {
                list.add(o);
            }
        }
        if (list.size() > 1) {
            Log.e("Duplicate Key", "More than one key matched your value.");
        } else {
            val = list.toString();
        }
        return val;
    }

}
