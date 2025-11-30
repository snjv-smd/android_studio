package com.example.posturemonitor;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StatsFragment extends Fragment {

    public StatsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayout grid = view.findViewById(R.id.calendar_grid);
        if (grid == null) return;

        int[] sample = new int[28]; // 0=no data,1=perfect,2=slouched

        int padding = dpToPx(6);
        int cellSize = dpToPx(40);
        grid.removeAllViews();

        for (int i = 0; i < 28; i++) {
            TextView tv = new TextView(requireContext());
            tv.setText(String.valueOf(i + 1));
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tv.setTextSize(12f);
            tv.setPadding(padding, padding, padding, padding);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = cellSize;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            tv.setLayoutParams(lp);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(8));
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.card_grey));

            if (sample[i] == 1) {
                bg.setColor(ContextCompat.getColor(requireContext(), R.color.green_dark));
                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else if (sample[i] == 2) {
                bg.setColor(ContextCompat.getColor(requireContext(), R.color.red_alert));
                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            }
            tv.setBackground(bg);

            final int dayIndex = i;
            tv.setOnClickListener(v -> {
                // placeholder for dialog or detail view
            });

            grid.addView(tv);
        }
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
