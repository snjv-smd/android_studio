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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

public class StatsFragment extends Fragment {

    private SessionStorage storage;

    public StatsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        storage = new SessionStorage(requireContext());
        populateCalendar(view);
        updateMetrics(view);
    }

    private void populateCalendar(View view) {
        GridLayout grid = view.findViewById(R.id.calendar_grid);
        if (grid == null) return;
        grid.removeAllViews();

        ArrayList<Session> sessions = storage.loadSessions();
        // Build a set of dayOfYear values that had slouches and those perfect
        HashSet<Integer> slouchedDays = new HashSet<>();
        HashSet<Integer> perfectDays = new HashSet<>();
        Calendar cal = Calendar.getInstance();
        long now = System.currentTimeMillis();

        for (Session s : sessions) {
            // compute day offset from today; only consider last 28 days
            long end = s.endMillis;
            int daysAgo = (int) ((now - end) / (1000L * 60 * 60 * 24));
            if (daysAgo < 0 || daysAgo >= 28) continue;
            cal.setTimeInMillis(end);
            int dayIndex = daysAgo; // 0 = today, 1 = yesterday, ...
            if (s.slouches > 0) slouchedDays.add(dayIndex);
            else perfectDays.add(dayIndex);
        }

        // create 28 cells (0..27) where 0 is today, 27 is 27 days ago
        for (int i = 27; i >= 0; i--) { // show older to top-left -> left-to-right
            TextView tv = new TextView(requireContext());
            tv.setText(String.valueOf(28 - i)); // simple numbering 1..28 (visual)
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light));
            tv.setTextSize(12f);
            int dp = dpToPx(40);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dp;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            tv.setLayoutParams(lp);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(8));
            // map index to daysAgo:
            int daysAgo = i;
            if (perfectDays.contains(daysAgo)) {
                bg.setColor(ContextCompat.getColor(requireContext(), R.color.green_dark));
                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else if (slouchedDays.contains(daysAgo)) {
                bg.setColor(ContextCompat.getColor(requireContext(), R.color.red_alert));
                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else {
                bg.setColor(ContextCompat.getColor(requireContext(), R.color.card_grey));
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_card_secondary));
            }
            tv.setBackground(bg);
            grid.addView(tv);
        }
    }

    private void updateMetrics(View view) {
        ArrayList<Session> sessions = storage.loadSessions();
        int bestStreak = 0;
        int currentStreak = 0;
        int totalSlouches = 0;
        long totalMillis = 0;
        int successDays = 0;
        int daysWithData = 0;

        long now = System.currentTimeMillis();
        boolean[] last28 = new boolean[28]; // true if perfect, false if slouched/no-data

        for (Session s : sessions) {
            long end = s.endMillis;
            int daysAgo = (int) ((now - end) / (1000L * 60 * 60 * 24));
            if (daysAgo < 0 || daysAgo >= 28) continue;
            daysWithData++;
            totalSlouches += s.slouches;
            totalMillis += s.getDurationMillis();
            if (s.slouches == 0) {
                successDays++;
                last28[daysAgo] = true;
            } else {
                last28[daysAgo] = false;
            }
        }

        // best streak: count longest contiguous sequence of perfect days in last28
        int streak = 0;
        for (int i = 0; i < 28; i++) {
            if (last28[i]) {
                streak++;
                if (streak > bestStreak) bestStreak = streak;
            } else {
                streak = 0;
            }
        }
        // current streak: count from today forward while perfect
        for (int i = 0; i < 28; i++) {
            if (last28[i]) currentStreak++;
            else break;
        }

        // success rate as percentage of perfect days among daysWithData
        double successRate = daysWithData == 0 ? 0.0 : (100.0 * successDays / daysWithData);

        TextView tvBest = view.findViewById(R.id.best_streak);
        TextView tvSuccess = view.findViewById(R.id.success_rate);
        TextView tvTotalTime = view.findViewById(R.id.total_time);
        TextView tvTotalSlouches = view.findViewById(R.id.total_slouches);
        TextView tvCurrent = view.findViewById(R.id.current_streak);

        tvBest.setText(String.valueOf(bestStreak));
        tvSuccess.setText(String.format("%.0f%%", successRate));
        tvTotalSlouches.setText(String.valueOf(totalSlouches));

        long totalSec = totalMillis / 1000;
        long mm = totalSec / 60;
        long ss = totalSec % 60;
        tvTotalTime.setText(String.format("%dm %ds", mm, ss));

        tvCurrent.setText(String.valueOf(currentStreak));
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
