package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static List<LatLng> addresses = new ArrayList<>();
    private static Map<String, String> keysUserNames = new HashMap<>();
    private static List<User> usersList = new ArrayList<>();
    private static List<String> adds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        addresses = (List<LatLng>) getIntent().getSerializableExtra("ADDRESSES");
        usersList = (List<User>) getIntent().getSerializableExtra("USERS");
        keysUserNames = (Map<String, String>) getIntent().getSerializableExtra("KEYSUSERNAMES");

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
        // Add a marker in Paris and move the camera
        LatLng Paris = new LatLng(48.866667, 2.333333);
        List<Marker> markers = new ArrayList<>();

        for (int i = 0; i < usersList.size(); i++) {
            markers.add(mMap.addMarker(new MarkerOptions().position(getLatLng(usersList.get(i).getAddress())).
                    title(usersList.get(i).getUsername()).snippet(usersList.get(i).getBloodType())));
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.moveCamera(cu);
        googleMap.animateCamera(cu);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
                String userName = marker.getTitle();
                String clickedMarkerUserId = getKeysFromValue(keysUserNames, userName); //get User Id from database
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
