package com.example.posturemonitor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MonitorFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_monitor);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_monitor) {
                selected = new MonitorFragment();
            } else if (id == R.id.nav_stats) {
                selected = new StatsFragment();
            } else if (id == R.id.nav_tips) {
                selected = new MonitorFragment(); // placeholder
            } else if (id == R.id.nav_settings) {
                selected = new MonitorFragment(); // placeholder
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selected)
                    .commit();
            return true;
        });
    }
}
