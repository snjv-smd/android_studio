package com.example.posturemonitor;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String[] permissions = {
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.BLUETOOTH_SCAN"
        };

        requestPermissions(permissions, 1001);

        super.onCreate(savedInstanceState);

        // Try to set the normal activity layout. If that fails we stop here with a toast.
        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load activity_main: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        container = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_nav);

        if (container == null || bottomNav == null) {
            Toast.makeText(this, "Required view id missing in activity_main.xml", Toast.LENGTH_LONG).show();
            // safe fallback: replace container with simple placeholder if possible
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, PlaceholderFragment.newInstance("Missing required IDs in activity_main.xml"))
                        .commit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        // Load default fragment (Monitor). If it throws, show the placeholder with the stack.
        if (savedInstanceState == null) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MonitorFragment())
                        .commit();
                bottomNav.setSelectedItemId(R.id.nav_monitor);
            } catch (Throwable t) {
                t.printStackTrace();
                showPlaceholder("Failed to load MonitorFragment:\n" + shortMsg(t));
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            try {
                if (id == R.id.nav_monitor) {
                    selected = new MonitorFragment();
                } else if (id == R.id.nav_stats) {
                    selected = new StatsFragment();
                } else if (id == R.id.nav_tips) {
                    selected = new TipsFragment();
                } else if (id == R.id.nav_settings) {
                    selected = new SettingsFragment();
                } else {
                    return false;
                }

                // Try replacing fragment. Catch any runtime throwable from fragment creation/inflation.
                try {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selected)
                            .commit();
                } catch (Throwable tx) {
                    tx.printStackTrace();
                    showPlaceholder("Failed to load " + item.getTitle() + ":\n" + shortMsg(tx));
                }
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                showPlaceholder("Error while handling nav click:\n" + shortMsg(t));
                return true;
            }
        });
    }

    private void showPlaceholder(String message) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, PlaceholderFragment.newInstance(message))
                    .commit();
            Toast.makeText(this, "Error: see screen", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to show placeholder: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // produce a short, readable message from throwable (type + first 3 lines of stack)
    private String shortMsg(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
        StackTraceElement[] st = t.getStackTrace();
        int lines = Math.min(3, st.length);
        for (int i = 0; i < lines; i++) {
            sb.append("  at ").append(st[i].toString()).append("\n");
        }
        return sb.toString();
    }
}
