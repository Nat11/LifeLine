package smartSystems.com.bloodBank.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import smartSystems.com.bloodBank.Activities.AdvancedSearchActivity;
import smartSystems.com.bloodBank.Activities.MapsActivity;
import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class SearchResultFragment extends ListFragment {

    private ListFragmentListener mListener;

    private static String searchedBloodType;
    private static int searchedDistance;
    private DatabaseReference mDatabase;
    private static String currentDonor, bloodType;
    private static String isDonor;
    private static String currentAddress;
    private static String address;
    private static LatLng currentLatLng;
    public static List<String> userNameBloodList = new ArrayList<>();
    private static List<User> usersList = new ArrayList<>();
    static Map<String, String> users = new HashMap<>();
    private static List<LatLng> addresses = new ArrayList<>();
    public static List<String> ids = new ArrayList<>();
    private static String userName;
    private static Map<String, String> keysUserNames = new HashMap<>();
    public static Map<String, User> userMap = new HashMap<>();
    private Button btnSwitchToMaps;
    ProgressDialog progressDialog;

    public SearchResultFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ListFragmentListener) {
            mListener = (ListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement ListFragmentListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle args = getArguments();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (args != null) {
            String bloodType = args.getString("searchedBloodType");
            int distance = args.getInt("searchedDistance");
            searchedBloodType = bloodType;
            searchedDistance = distance;
            loadData(searchedBloodType, searchedDistance);
        }
    }

    public static SearchResultFragment newInstance(String arg1, int arg2) {

        Bundle args = new Bundle();
        args.putString("searchedBloodType", arg1);
        args.putInt("searchedDistance", arg2);
        SearchResultFragment fragment = new SearchResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        btnSwitchToMaps = (Button) view.findViewById(R.id.btnSwitchToMaps);
        return view;
    }

    public void loadData(final String mBloodType, final int mDistance) {
        Log.d("searched blood ", searchedBloodType);

        final FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child("users").child(current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clearLists();

                if (dataSnapshot.hasChildren()) {
                    Map<String, String> values = (Map<String, String>) dataSnapshot.getValue();
                    currentDonor = values.get("donor");
                    currentAddress = values.get("address");//Get address of current user
                    currentLatLng = getLatLng(currentAddress); //Convert address to Latitude Longitude
                }

                if (currentDonor.equals("Yes"))
                    isDonor = "No";
                else
                    isDonor = "Yes";

                mDatabase.child("users").orderByChild("donor").equalTo(isDonor).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            userName = user.getUsername();
                            address = user.getAddress();
                            bloodType = user.getBloodType();
                            double distance = SphericalUtil.computeDistanceBetween(currentLatLng, getLatLng(address));
                            if (bloodType.equals(mBloodType) && distance / 1000 < mDistance) { //divise by 1000 to get distance in Kilometers
                                users.put(snapshot.getKey(), user.getBloodType() + " : " + user.getUsername()); //display searchedBloodType and username in ListView
                                addresses.add(getLatLng(address));
                                userMap.put(snapshot.getKey(), user);
                                keysUserNames.put(snapshot.getKey(), userName);
                                usersList.add(user);
                            }
                        }
                        if (users.size() == 0) {
                            Toast.makeText(getActivity(), "Invalid search criteria, please change your choices", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getActivity(), AdvancedSearchActivity.class));

                        } else {
                            userNameBloodList = new ArrayList<>(users.values());
                            Collections.sort(userNameBloodList);//Sort userNameBloodList by values
                            ids = new ArrayList<>(users.keySet());
                        }
                        Log.d("test users size", String.valueOf(userNameBloodList.size()));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                android.R.layout.simple_list_item_1,
                                userNameBloodList);
                        setListAdapter(adapter);

                        btnSwitchToMaps.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                goToMaps(addresses, usersList, keysUserNames);
                            }
                        });
                        progressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("UserInfo", "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String userId = ids.get(position);
        mListener.onListItemClick(userId);
    }

    public interface ListFragmentListener {
        void onListItemClick(String id);
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

    public void clearLists() {
        users.clear();
        userNameBloodList.clear();
        addresses.clear();
    }

    public void goToMaps(List<LatLng> addresses, List<User> usersList, Map<String, String> keysUserNames) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        intent.putExtra("ADDRESSES", (Serializable) addresses);
        intent.putExtra("KEYSUSERNAMES", (Serializable) keysUserNames);
        intent.putExtra("USERS", (Serializable) usersList);
        startActivity(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
