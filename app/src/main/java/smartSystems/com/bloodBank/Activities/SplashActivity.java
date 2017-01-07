package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import smartSystems.com.bloodBank.R;

public class SplashActivity extends ActionBarActivity {

    @InjectView(R.id.loading_image)
    protected ImageView animationImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.inject(this);

        AnimationDrawable animationDrawable = (AnimationDrawable) animationImage.getDrawable();
        animationDrawable.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);
    }
}
