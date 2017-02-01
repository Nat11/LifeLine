package smartSystems.com.bloodBank.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import smartSystems.com.bloodBank.Model.ObjectSerializer;
import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

import static android.R.attr.key;

public class DefaultMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static List<User> matchedUsers = new ArrayList<>();
    private static Map<String, String> keyUserNames = new HashMap<>();

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

        matchedUsers = (List<User>) getIntent().getSerializableExtra("USERS");
        keyUserNames = (Map<String, String>) getIntent().getSerializableExtra("KEYUSERNAMES");

        if (null == matchedUsers || null == keyUserNames) {
            matchedUsers = new ArrayList<User>();
            keyUserNames = new HashMap<String, String>();

            // load data from preference
            SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);

            try {
                matchedUsers = (ArrayList<User>) ObjectSerializer.deserialize(prefs.getString("matchedUsers", ObjectSerializer.serialize(new ArrayList<User>())));
                keyUserNames = (HashMap<String, String>) ObjectSerializer.deserialize(prefs.getString("keyUsernames", ObjectSerializer.serialize(new HashMap<String, String>())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        setUpMapIfNeeded();
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
                String clickedMarkerUserId = getKeysFromValue(keyUserNames, userName); //get User Id from database
                String result = clickedMarkerUserId.substring(1, clickedMarkerUserId.length() - 1);
                intent.putExtra("id", result);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save the task list to preference
        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("matchedUsers", ObjectSerializer.serialize((Serializable) matchedUsers));
            editor.putString("keyUsernames", ObjectSerializer.serialize((Serializable) keyUserNames));

        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == matchedUsers || null == keyUserNames) {
            matchedUsers = new ArrayList<User>();
            keyUserNames = new HashMap<String, String>();
            // load tasks from preference
            SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);

            try {
                matchedUsers = (ArrayList<User>) ObjectSerializer.deserialize(prefs.getString("matchedUsers", ObjectSerializer.serialize(new ArrayList<User>())));
                keyUserNames = (HashMap<String, String>) ObjectSerializer.deserialize(prefs.getString("keyUsernames", ObjectSerializer.serialize(new HashMap<String, String>())));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        setUpMapIfNeeded();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DefaultMapsActivity.this, UserActivity.class));
    }

}
