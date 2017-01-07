package smartSystems.com.bloodBank.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import smartSystems.com.bloodBank.R;

public class SearchActivity extends AppCompatActivity {

    private Button btnQuickSearch, btnAdvancedSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        btnQuickSearch = (Button) findViewById(R.id.btnQuickSearch);
        btnAdvancedSearch = (Button) findViewById(R.id.btnAdvancedSearch);

        btnQuickSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, QuickSearchActivity.class));
            }
        });

        btnAdvancedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, AdvancedSearchActivity.class));
            }
        });
    }
}
