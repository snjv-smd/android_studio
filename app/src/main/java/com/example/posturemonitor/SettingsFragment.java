package com.example.posturemonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private static final String PREFS = "posture_prefs";
    private static final String KEY_BODY = "body_weight";
    private static final String KEY_BACKPACK = "backpack_weight";
    private static final String KEY_NOTIF = "notifications_on";
    private static final String KEY_VIB = "vibration_on";
    private static final String KEY_SOUND = "sound_on";
    private static final String KEY_SENS = "sensitivity";

    private SharedPreferences prefs;

    public SettingsFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = requireContext().getSharedPreferences(PREFS, 0);

        EditText etBody = view.findViewById(R.id.et_body_weight);
        EditText etBackpack = view.findViewById(R.id.et_backpack_weight);
        SwitchCompat swNotif = view.findViewById(R.id.switch_notifications);
        SwitchCompat swVib = view.findViewById(R.id.switch_vibration);
        SwitchCompat swSound = view.findViewById(R.id.switch_sound);
        SeekBar seek = view.findViewById(R.id.seek_sensitivity);
        TextView tvSensitivity = view.findViewById(R.id.tv_sensitivity_value);
        TextView tvVersion = view.findViewById(R.id.tv_version);
        TextView tvBuild = view.findViewById(R.id.tv_build);

        // load saved values
        etBody.setText(String.valueOf(prefs.getFloat(KEY_BODY, 70f)));
        etBackpack.setText(String.valueOf(prefs.getFloat(KEY_BACKPACK, 0f)));
        swNotif.setChecked(prefs.getBoolean(KEY_NOTIF, true));
        swVib.setChecked(prefs.getBoolean(KEY_VIB, true));
        swSound.setChecked(prefs.getBoolean(KEY_SOUND, false));
        int sens = prefs.getInt(KEY_SENS, 5);
        seek.setProgress(sens);
        tvSensitivity.setText(sens + "/10");

        // version/build (static here, you can read from package info if preferred)
        tvVersion.setText("1.0.0");
        tvBuild.setText("2025.10.20");

        // listeners: persist changes
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSensitivity.setText(progress + "/10");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                prefs.edit().putInt(KEY_SENS, seekBar.getProgress()).apply();
            }
        });

        swNotif.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_NOTIF, isChecked).apply());

        swVib.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_VIB, isChecked).apply());

        swSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_SOUND, isChecked).apply());

        // save numeric inputs when user leaves field (simple)
        etBody.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveFloatFromEditText(etBody, KEY_BODY);
        });
        etBackpack.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveFloatFromEditText(etBackpack, KEY_BACKPACK);
        });
    }

    private void saveFloatFromEditText(EditText et, String key) {
        String s = et.getText().toString().trim();
        if (TextUtils.isEmpty(s)) return;
        try {
            float val = Float.parseFloat(s);
            prefs.edit().putFloat(key, val).apply();
        } catch (NumberFormatException ignored) { }
    }
}
