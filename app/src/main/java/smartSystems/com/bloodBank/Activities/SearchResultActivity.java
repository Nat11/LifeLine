package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;

import smartSystems.com.bloodBank.Fragments.DetailActivityFragment;
import smartSystems.com.bloodBank.Fragments.SearchResultFragment;
import smartSystems.com.bloodBank.R;

public class SearchResultActivity extends AppCompatActivity implements SearchResultFragment.ListFragmentListener {

    private static final String TAG = "SearchResultActivity";
    private String searchedBloodType;
    private int searchedDistance;
    private boolean mTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        searchedBloodType = getIntent().getStringExtra("blood");
        searchedDistance = getIntent().getIntExtra("distance", 0);

        SearchResultFragment fragment = SearchResultFragment.newInstance(searchedBloodType, searchedDistance);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.user_list_fragment, fragment)
                .commit();

        //check if app is running on phone or tablet
        FrameLayout fragmentContainer = (FrameLayout) findViewById(R.id.detail_fragment_container);
        mTablet = (fragmentContainer != null);
        Log.i(TAG, "onCreate: mTablet" + mTablet);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SearchResultActivity.this, AdvancedSearchActivity.class));
    }

    @Override
    public void onListItemClick(String id) {

        if (mTablet) {
            DetailActivityFragment fragment = DetailActivityFragment.newInstance(id);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_fragment_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(SearchResultActivity.this, DetailActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }
    }
}
