package com.example.posturemonitor;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_MSG = "arg_msg";

    public static PlaceholderFragment newInstance(String message) {
        PlaceholderFragment f = new PlaceholderFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MSG, message);
        f.setArguments(b);
        return f;
    }

    public PlaceholderFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setPadding(32, 32, 32, 32);
        tv.setTextSize(14f);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        tv.setBackgroundColor(0xFF12151A);
        tv.setMovementMethod(new ScrollingMovementMethod());
        String msg = (getArguments() != null) ? getArguments().getString(ARG_MSG) : "Unknown error";
        tv.setText(msg);
        return tv;
    }
}
