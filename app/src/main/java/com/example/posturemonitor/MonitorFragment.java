package com.example.posturemonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothDevice;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;


public class MonitorFragment extends Fragment {

    private static final String TAG = "MonitorFragment";

    // Bluetooth
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private InputStream btInput;
    private boolean btConnected = false;

    private final UUID HC05_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private TextView tvTime;
    private TextView tvSlouches;
    private TextView tvGoodPercent;
    private Button btnStart;
    private Button btnRetry;

    private boolean running = false;
    private long startTimeMs = 0L;
    private long elapsedBeforeMs = 0L;
    private final Handler handler = new Handler();
    private final Random rng = new Random();
    private int slouches = 0;

    private SessionStorage storage;

    // colors (load after view exists)
    private int colorOn;
    private int colorOff;



    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long now = SystemClock.elapsedRealtime();
            long elapsed = (now - startTimeMs) + elapsedBeforeMs;
            updateTimeText(elapsed);
            updateGoodPercent(elapsed, slouches);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monitor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // find views defensively
        tvTime = safeFindTextView(view, R.id.session_time);
        tvSlouches = safeFindTextView(view, R.id.slouches_count);
        tvGoodPercent = safeFindTextView(view, R.id.good_percent);
        btnStart = safeFindButton(view, R.id.start_monitor_btn);
        btnRetry = safeFindButton(view, R.id.retry_btn);

        storage = new SessionStorage(requireContext());

        //bluetooth
        connectToHC05();

        // load colors safely (only if context available)
        try {
            colorOn = ContextCompat.getColor(requireContext(), R.color.monitor_on);
            colorOff = ContextCompat.getColor(requireContext(), R.color.monitor_off);
        } catch (Exception e) {
            // fallback if colors missing
            colorOn = 0xFF2ECC71;
            colorOff = 0xFF555555;
            Log.w(TAG, "Using fallback colors for monitor button", e);
        }

        // initial UI
        updateTimeText(elapsedBeforeMs);
        if (tvSlouches != null) tvSlouches.setText(String.valueOf(slouches));
        updateGoodPercent(elapsedBeforeMs, slouches);

        // set initial tint only if button exists
        if (btnStart != null) {
            btnStart.setText("Start Monitoring");
            btnStart.setBackgroundTintList(ColorStateList.valueOf(colorOff));
        } else {
            Log.w(TAG, "start button not found (R.id.start_monitor_btn)");
        }

        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                if (!running) startMonitoring();
                else stopMonitoring();
            });
        }

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                if (running) {
                    slouches = 0;
                    elapsedBeforeMs = 0;
                    startTimeMs = SystemClock.elapsedRealtime();
                    if (tvSlouches != null) tvSlouches.setText("0");
                    updateGoodPercent(0, 0);
                    Toast.makeText(requireContext(), "Session reset", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Start monitoring first", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startMonitoring() {
        running = true;
        slouches = 0;
        if (tvSlouches != null) tvSlouches.setText("0");
        updateGoodPercent(0, 0);
        startTimeMs = SystemClock.elapsedRealtime();
        handler.post(tick);

        if (btnStart != null) {
            btnStart.setText("Stop Monitoring");
            btnStart.setBackgroundTintList(ColorStateList.valueOf(colorOn));
        }
        Toast.makeText(requireContext(), "Monitoring started", Toast.LENGTH_SHORT).show();
    }

    private void stopMonitoring() {
        running = false;
        handler.removeCallbacks(tick);
        long now = SystemClock.elapsedRealtime();
        long elapsed = (now - startTimeMs) + elapsedBeforeMs;
        updateTimeText(elapsed);

        if (btnStart != null) {
            btnStart.setText("Start Monitoring");
            btnStart.setBackgroundTintList(ColorStateList.valueOf(colorOff));
        }
        Toast.makeText(requireContext(), "Monitoring stopped", Toast.LENGTH_SHORT).show();

        long startWall = System.currentTimeMillis() - elapsed;
        Session s = new Session(startWall, System.currentTimeMillis(), slouches);
        storage.saveSession(s);

        elapsedBeforeMs = 0L;
    }

    private void updateTimeText(long millis) {
        int totalSec = (int) (millis / 1000);
        int mm = totalSec / 60;
        int ss = totalSec % 60;
        if (tvTime != null) tvTime.setText(String.format("%02d:%02d", mm, ss));
    }

    private void updateGoodPercent(long millis, int slouches) {
        double percent = 100.0 - (slouches * 5.0);
        if (percent < 0) percent = 0;
        if (tvGoodPercent != null) tvGoodPercent.setText(String.format("%.1f%%", percent));
    }

    // safe helpers that avoid NPEs and log if a view is missing
    private TextView safeFindTextView(View root, int id) {
        if (root == null) return null;
        try {
            View v = root.findViewById(id);
            if (v instanceof TextView) return (TextView) v;
            return null;
        } catch (Exception e) {
            Log.w(TAG, "safeFindTextView failed for id=" + id, e);
            return null;
        }
    }

    private Button safeFindButton(View root, int id) {
        if (root == null) return null;
        try {
            View v = root.findViewById(id);
            if (v instanceof Button) return (Button) v;
            return null;
        } catch (Exception e) {
            Log.w(TAG, "safeFindButton failed for id=" + id, e);
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (running) {
            handler.removeCallbacks(tick);
            long now = SystemClock.elapsedRealtime();
            elapsedBeforeMs += (now - startTimeMs);
            running = false;
            if (btnStart != null) btnStart.setText("Start Monitoring");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void connectToHC05() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    1001);

            return; // STOP HERE — wait for user to accept permission
        }


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
            return;
        }

        BluetoothDevice hc05 = null;
        for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
            if (dev.getName().contains("HC")) {
                hc05 = dev;
                break;
            }
        }

        if (hc05 == null) {
            Toast.makeText(getContext(), "Pair HC-05 first", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            btSocket = hc05.createRfcommSocketToServiceRecord(HC05_UUID);
            btSocket.connect();
            btInput = btSocket.getInputStream();
            btConnected = true;

            Toast.makeText(getContext(), "HC-05 Connected!", Toast.LENGTH_SHORT).show();

            startBluetoothReader();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to connect: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startBluetoothReader() {
        Thread t = new Thread(() -> {
            byte[] buffer = new byte[256];
            int bytes;

            while (btConnected) {
                try {
                    bytes = btInput.read(buffer);
                    String incoming = new String(buffer, 0, bytes).trim();

                    requireActivity().runOnUiThread(() -> handleBluetoothData(incoming));

                } catch (Exception e) {
                    btConnected = false;
                }
            }
        });

        t.start();
    }

    private void handleBluetoothData(String msg) {

        // Example messages from Arduino:
        // SLOUCH
        // GOOD
        // PITCH:12
        // IR:432

        if (!running) return;

        if (msg.equals("SLOUCH")) {
            slouches++;
            if (tvSlouches != null) tvSlouches.setText(String.valueOf(slouches));
        }

        if (msg.startsWith("PITCH:")) {
            // Example: update UI or store pitch
        }

        if (msg.startsWith("IR:")) {
            // Example: show IR value somewhere
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            btConnected = false;
            if (btSocket != null) btSocket.close();
        } catch (Exception ignored) {}
    }

}
