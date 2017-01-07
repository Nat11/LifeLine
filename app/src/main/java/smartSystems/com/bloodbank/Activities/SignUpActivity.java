package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.Places.PlaceArrayAdapter;
import smartSystems.com.bloodBank.R;
import smartSystems.com.bloodBank.Session.Session;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private final String TAG = "FB_SIGNUP";
    private static final String LOG_TAG = "SignUpActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText etPass,etEmail,etPhone;
    private RadioGroup etRadioGroup;
    private RadioButton etGender;
    private Spinner etBloodType;
    private Button etCreate;
    private Session session;
    final String[] bloodType = new String[1];
    private CheckBox etDonor;

    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteAddress; //Address auto complete google API Places
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();
        etEmail = (EditText) findViewById(R.id.User);
        etPass = (EditText) findViewById(R.id.Password);
        session = new Session(this);
        //etAddress = (EditText) findViewById(R.id.Address);
        mAutocompleteAddress = (AutoCompleteTextView) findViewById(R.id
                .AutoCompleteAddress);
        etPhone = (EditText) findViewById(R.id.Phone);
        etRadioGroup = (RadioGroup) findViewById(R.id.Gender);
        etCreate = (Button) findViewById(R.id.btnRegister);
        etBloodType = (Spinner) findViewById(R.id.BloodType);
        etDonor = (CheckBox) findViewById(R.id.Donor);

        etBloodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bloodType[0] = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(SignUpActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mAutocompleteAddress.setThreshold(3);
        mAutocompleteAddress.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteAddress.setAdapter(mPlaceArrayAdapter);
        findViewById(R.id.btnRegister).setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                createUserAccount();
                break;
        }
    }

    private boolean checkFormFields() {
        String email, password, address, phone;
        email = etEmail.getText().toString();
        password = etPass.getText().toString();
        address = mAutocompleteAddress.getText().toString();
        phone = etPhone.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()) {
            etPass.setError("Password Required");
            return false;
        }
        if (address.isEmpty()) {
            mAutocompleteAddress.setError("Address required");
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone number required");
        }

        return true;
    }

    private void createUserAccount() {
        if (!checkFormFields())
            return;

        final String email = etEmail.getText().toString();
        final String password = etPass.getText().toString();
        final String address = mAutocompleteAddress.getText().toString();
        final String phone = etPhone.getText().toString();
        final int gender = etRadioGroup.getCheckedRadioButtonId();
        etGender = (RadioButton) findViewById(gender);
        final String donor;
        if (etDonor.isChecked()) {
            donor = "Yes";
        } else donor = "No";

        /*Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address location = addressList.get(0);
        Toast.makeText(this, "Lat " + location.getLatitude() + " Long " + location.getLongitude(), Toast.LENGTH_SHORT).show();
*/

        // Create the user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(SignUpActivity.this, "User created", Toast.LENGTH_SHORT)
                                    // .show();
                                    session.setLoggedIn(true);
                                    FirebaseUser FbUser = task.getResult().getUser();
                                    User user = new User(email, password, address, phone, (String) etGender.getText(), bloodType[0], donor);
                                    mDatabase.child("users").child(FbUser.getUid()).setValue(user);
                                    Toast.makeText(SignUpActivity.this, "Account created", Toast.LENGTH_LONG);
                                    startActivity(new Intent(SignUpActivity.this, UserActivity.class));
                                    finish();

                                } else {
                                    Toast.makeText(SignUpActivity.this, "Account creation failed", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }/////
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, "This email address is already in use.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
