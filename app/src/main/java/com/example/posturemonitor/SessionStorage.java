package com.example.posturemonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SessionStorage {
    private static final String PREFS = "posture_prefs";
    private static final String KEY_SESSIONS = "sessions_json";
    private final SharedPreferences prefs;

    public SessionStorage(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveSession(Session s) {
        ArrayList<Session> list = loadSessions();
        list.add(0, s); // newest first
        saveList(list);
    }

    private void saveList(ArrayList<Session> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Session s : list) {
                JSONObject o = new JSONObject();
                o.put("start", s.startMillis);
                o.put("end", s.endMillis);
                o.put("slouches", s.slouches);
                arr.put(o);
            }
            prefs.edit().putString(KEY_SESSIONS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Session> loadSessions() {
        String raw = prefs.getString(KEY_SESSIONS, "");
        ArrayList<Session> out = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                long start = o.optLong("start", 0);
                long end = o.optLong("end", start);
                int sl = o.optInt("slouches", 0);
                out.add(new Session(start, end, sl));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return out;
    }
}
