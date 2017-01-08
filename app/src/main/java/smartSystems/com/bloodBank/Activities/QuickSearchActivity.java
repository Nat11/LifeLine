package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Toast;

import smartSystems.com.bloodBank.Model.User;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartSystems.com.bloodBank.R;

public class QuickSearchActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    static Map<String, String> users = new HashMap<>();
    public static final String USER = "USER";
    public static final String ADDRESSES = "ADDRESSES";
    public static List<String> userNames = new ArrayList<>();
    public static List<LatLng> addresses = new ArrayList<>();
    public static List<String> ids = new ArrayList<>();
    private EditText etSearch, etDistance;
    ArrayAdapter<String> adapter;
    private static String donor, currentBloodType, bloodType;
    private static String currentAddress;
    private static String address;
    private static String maxD;
    private static double maxDistance;
    private static LatLng currentLatLng;
    private Button btnMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_search);
        //mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final ListView lv = (ListView) findViewById(R.id.listView);
        //etSearch = (EditText) findViewById(R.id.etSearch);
        //etSearch.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // make first character uppercase to match bloodtype in database
        //btnSearchDistance = (Button) findViewById(R.id.btnSearchDistance);
        etDistance = (EditText) findViewById(R.id.etMaxDistance);
        btnMaps = (Button) findViewById(R.id.btnMaps);

        final FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        etDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearLists();
                lv.setAdapter(null);
                btnMaps.setVisibility(View.INVISIBLE);

                /*if (Double.isNaN(Double.parseDouble(String.valueOf(charSequence)))) {
                    etDistance.setError("False entry");
                    return;
                }*/

                /*try {
                    maxDistance = new Double(etDistance.getText().toString());
                } catch (NumberFormatException e) {
                    maxDistance = 5.0;
                }
                */

                try {
                    maxDistance = Double.parseDouble(String.valueOf(charSequence));
                } catch (NumberFormatException e) {
                    lv.setVisibility(View.INVISIBLE);
                    btnMaps.setVisibility(View.INVISIBLE);
                    return;
                }

                lv.setVisibility(View.VISIBLE);

                if (charSequence.length() == 0) {
                    btnMaps.setVisibility(View.INVISIBLE);
                    return;
                }

                if (charSequence.length() > 0) {
                    clearLists();

                    mDatabase.child("users").child(current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                Map<String, String> values = (Map<String, String>) dataSnapshot.getValue();
                                donor = values.get("donor");
                                currentBloodType = values.get("bloodType");
                                currentAddress = values.get("address");//Get address of current user
                                currentLatLng = getLatLng(currentAddress); //Convert address to Latitude Longitude
                            }

                            if (donor.equals("Yes")) {
                                mDatabase.child("users").orderByChild("donor").equalTo("No").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        clearLists();

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            User user = snapshot.getValue(User.class);
                                            address = user.getAddress();
                                            bloodType = user.getBloodType();
                                            double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                            if (distance / 1000 < maxDistance) { //divise by 1000 to get distance in Kilometers
                                                users.put(snapshot.getKey(), user.getBloodType() + " : " + user.getUsername()); //display bloodType and username in ListView
                                                addresses.add(getLatLng(address));
                                            }
                                        }
                                        if (users.size() == 0) {
                                            lv.setVisibility(View.INVISIBLE);
                                        }
                                        btnMaps.setVisibility(View.VISIBLE);
                                        userNames = new ArrayList<String>(users.values());
                                        Collections.sort(userNames);//Sort userNames by values
                                        adapter = new ArrayAdapter<>(
                                                QuickSearchActivity.this,
                                                android.R.layout.simple_list_item_1,
                                                userNames);

                                        ids = new ArrayList<String>(users.keySet());
                                        lv.setAdapter(adapter);
                                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                Intent intent = new Intent(QuickSearchActivity.this, DetailActivity.class);
                                                String id = ids.get(i);
                                                intent.putExtra(USER, id);
                                                startActivity(intent);
                                            }
                                        });

                                        btnMaps.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (addresses.size() == 0) {
                                                    Toast.makeText(QuickSearchActivity.this, R.string.search_error_donor, Toast.LENGTH_SHORT).show();
                                                }
                                                goToMaps(addresses);
                                            }
                                        });
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
                                        clearLists();

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            User user = snapshot.getValue(User.class);
                                            address = user.getAddress();
                                            double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                                            if (distance / 1000 < maxDistance) { //divise by 1000 to get distance in Kilometers
                                                users.put(snapshot.getKey(), user.getUsername());
                                                addresses.add(getLatLng(address));
                                            }
                                        }
                                        if (users.size() == 0) {
                                            lv.setVisibility(View.INVISIBLE);
                                        }

                                        btnMaps.setVisibility(View.VISIBLE);
                                        userNames = new ArrayList<String>(users.values());
                                        adapter = new ArrayAdapter<>(
                                                QuickSearchActivity.this,
                                                android.R.layout.simple_list_item_1,
                                                userNames);

                                        ids = new ArrayList<String>(users.keySet());
                                        lv.setAdapter(adapter);
                                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                Intent intent = new Intent(QuickSearchActivity.this, DetailActivity.class);
                                                String id = ids.get(i);
                                                intent.putExtra(USER, id);
                                                startActivity(intent);
                                            }
                                        });

                                        btnMaps.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (addresses.size() == 0) {
                                                    Toast.makeText(QuickSearchActivity.this, R.string.search_error_donor, Toast.LENGTH_SHORT).show();
                                                } else
                                                    goToMaps(addresses);
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
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("UserInfo", "getUser:onCancelled", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void goToMaps(List<LatLng> addresses) {
        Intent intent = new Intent(QuickSearchActivity.this, MapsActivity.class);
        intent.putExtra(ADDRESSES, (Serializable) addresses);
        startActivity(intent);
    }

    public LatLng getLatLng(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address location = addressList.get(0);
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void clearLists() {
        users.clear();
        userNames.clear();
        addresses.clear();
    }
}
