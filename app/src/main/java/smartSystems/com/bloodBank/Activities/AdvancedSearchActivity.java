package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import smartSystems.com.bloodBank.R;

public class AdvancedSearchActivity extends AppCompatActivity implements View.OnClickListener {

    private SeekBar distance = null;
    private TextView txtMaxDistance;
    private static int progressChanged = 0;
    private Button btnSearch;
    private Spinner etBloodType;
    final String[] bloodType = new String[1];
    private static int distanceSearched = 100; //Max value by default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);
        distance = (SeekBar) findViewById(R.id.distanceBar);
        txtMaxDistance = (TextView) findViewById(R.id.txtMaxDistance);
        btnSearch = (Button) findViewById(R.id.btnBeginSearch);
        etBloodType = (Spinner) findViewById(R.id.searchBloodType);

        distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                txtMaxDistance.setText(String.valueOf(progressChanged));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                distanceSearched = progressChanged;
            }
        });

        etBloodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bloodType[0] = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBeginSearch:
                goToResultActivity();
                break;
        }
    }

    private void goToResultActivity() {
        String blood = bloodType[0];
        int distance = distanceSearched;

        Intent intent = new Intent(AdvancedSearchActivity.this, SearchResultActivity.class);
        intent.putExtra("distance", distance);
        intent.putExtra("blood", blood);
        startActivity(intent);
    }
}
