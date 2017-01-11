package smartSystems.com.bloodBank.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import smartSystems.com.bloodBank.Places.PlaceArrayAdapter;
import smartSystems.com.bloodBank.R;
import smartSystems.com.bloodBank.Session.Session;

public class UpdateInfoActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String LOG_TAG = "UpdateInfoActivity";
    private static final String TAG = "Donor";
    private static DatabaseReference mDatabase;
    private static FirebaseAuth mAuth;
    private Session session;
    private Button btnUpdate;
    private ProgressDialog progressDialog;
    private EditText etNewAddress, etNewPhone;
    private CheckBox newDonor;
    private static String newAddress, newPhone, newIsDonor;
    private static boolean isDonor = false;
    private static FirebaseUser user;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteAddress; //Address auto complete google API Places
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        session = new Session(this);
        if (!session.loggedIn()) {
            logout();
        }
        user = mAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        mAutocompleteAddress = (AutoCompleteTextView) findViewById(R.id.etNewAddress);
        etNewPhone = (EditText) findViewById(R.id.etNewPhone);
        newDonor = (CheckBox) findViewById(R.id.newDonor);
        btnUpdate = (Button) findViewById(R.id.btnUpdateInfo);

        progressDialog.show();

        mDatabase.child("users").child(user.getUid()).child("donor").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newIsDonor = (String) dataSnapshot.getValue();
                        if (newIsDonor.equals("Yes")) {
                            isDonor = true;
                            newDonor.setText(R.string.DonorStatus);
                        } else {
                            isDonor = false;
                            newDonor.setText(R.string.NonDonorStatus);
                        }

                        Log.i(TAG, String.valueOf(isDonor));
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        mGoogleApiClient = new GoogleApiClient.Builder(UpdateInfoActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mAutocompleteAddress.setThreshold(3);
        mAutocompleteAddress.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteAddress.setAdapter(mPlaceArrayAdapter);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newAddress = mAutocompleteAddress.getText().toString();
                newPhone = etNewPhone.getText().toString();

                if (!newAddress.isEmpty())
                    update("address", newAddress);

                if (!newPhone.isEmpty())
                    update("phone", newPhone);

                if (newDonor.isChecked()) {
                    if (isDonor)
                        update("donor", "No");
                    else if (!isDonor)
                        update("donor", "Yes");
                }

                if (newAddress.isEmpty() && newPhone.isEmpty() && !newDonor.isChecked())
                    Toast.makeText(UpdateInfoActivity.this, R.string.back_update, Toast.LENGTH_SHORT).show();

                if (!newAddress.isEmpty() || !newPhone.isEmpty() || newDonor.isChecked()) {
                    startActivity(new Intent(UpdateInfoActivity.this, UserActivity.class));
                    finish();
                }
            }
        });

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mDatabase.child("users").child(user.getUid()).child("donor").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newIsDonor = (String) dataSnapshot.getValue();
                        if (newIsDonor.equals("Yes")) {
                            isDonor = true;
                            newDonor.setText(R.string.DonorStatus);
                        } else {
                            newDonor.setText(R.string.NonDonorStatus);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static void update(String child, String value) {
        mDatabase.child("users").child(user.getUid()).child(child).setValue(value);
    }

    private void logout() {
        session.setLoggedIn(false);
        finish();
        startActivity(new Intent(UpdateInfoActivity.this, LoginActivity.class));
    }

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }
}
