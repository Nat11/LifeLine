package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class LoadingScreenActivity extends AppCompatActivity {
    //Introduce an delay
    private final int WAIT_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Simulating a long running task
      /* Create an Intent that will start the ProfileData-Activity. */
                Intent mainIntent = new Intent(LoadingScreenActivity.this, UserActivity.class);
                LoadingScreenActivity.this.startActivity(mainIntent);
                LoadingScreenActivity.this.finish();
            }
        }, WAIT_TIME);
    }
}
