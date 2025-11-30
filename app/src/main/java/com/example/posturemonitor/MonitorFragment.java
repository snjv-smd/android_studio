package com.example.posturemonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class MonitorFragment extends Fragment {

    private TextView tvTime;
    private TextView tvSlouches;
    private TextView tvGoodPercent;
    private FloatingActionButton fab;
    private boolean running = false;

    private long startTimeMs;
    private long elapsedBeforeMs = 0;
    private final Handler handler = new Handler();
    private final Random random = new Random();
    private int slouches = 0;

    private SessionStorage storage;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long now = SystemClock.elapsedRealtime();
            long elapsed = (now - startTimeMs) + elapsedBeforeMs;
            updateTimeText(elapsed);
            // simulate slouch event randomly based on elapsed seconds
            int chance = random.nextInt(1000);
            if (chance < 12) { // ~1.2% per tick -> occasional slouches
                slouches++;
                tvSlouches.setText(String.valueOf(slouches));
            }
            updateGoodPercent(elapsed, slouches);
            handler.postDelayed(this, 1000);
        }
    };

    private void updateTimeText(long millis) {
        int totalSec = (int) (millis / 1000);
        int mm = totalSec / 60;
        int ss = totalSec % 60;
        tvTime.setText(String.format("%02d:%02d", mm, ss));
    }

    private void updateGoodPercent(long millis, int slouches) {
        // simple metric: goodPercent = max(0, 100 - slouches * 5) clamped
        double percent = 100.0 - (slouches * 5.0);
        if (percent < 0) percent = 0;
        tvGoodPercent.setText(String.format("%.1f%%", percent));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monitor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvTime = view.findViewById(R.id.session_time);
        tvSlouches = view.findViewById(R.id.slouches_count);
        tvGoodPercent = view.findViewById(R.id.good_percent);
        fab = view.findViewById(R.id.fab_start_stop);

        storage = new SessionStorage(requireContext());

        fab.setOnClickListener(v -> {
            if (!running) startMonitoring();
            else stopMonitoring();
        });
    }

    private void startMonitoring() {
        running = true;
        slouches = 0;
        tvSlouches.setText("0");
        tvGoodPercent.setText("100.0%");
        startTimeMs = SystemClock.elapsedRealtime();
        handler.post(tick);
        fab.setImageResource(android.R.drawable.ic_media_pause);
        Toast.makeText(requireContext(), "Monitoring started", Toast.LENGTH_SHORT).show();
    }

    private void stopMonitoring() {
        running = false;
        handler.removeCallbacks(tick);
        long endMs = SystemClock.elapsedRealtime();
        long elapsed = (endMs - startTimeMs) + elapsedBeforeMs;
        updateTimeText(elapsed);
        fab.setImageResource(android.R.drawable.ic_media_play);
        Toast.makeText(requireContext(), "Monitoring stopped", Toast.LENGTH_SHORT).show();

        // save session
        long startWall = System.currentTimeMillis() - elapsed; // approximate wall-clock start
        Session s = new Session(startWall, System.currentTimeMillis(), slouches);
        storage.saveSession(s);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (running) {
            // stop and keep elapsed so user can resume
            handler.removeCallbacks(tick);
            long now = SystemClock.elapsedRealtime();
            elapsedBeforeMs += (now - startTimeMs);
            running = false;
            fab.setImageResource(android.R.drawable.ic_media_play);
        }
    }
}
