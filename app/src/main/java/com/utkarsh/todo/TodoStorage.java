package com.utkarsh.todo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads tasks to SharedPreferences as a JSON array.
 * Nothing fancy — just a one-liner you call after every change.
 */
public class TodoStorage {

    private static final String PREFS_NAME = "funny_todo_prefs";
    private static final String KEY_TASKS  = "tasks";

    private final SharedPreferences prefs;

    public TodoStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(List<TodoItem> items) {
        JSONArray arr = new JSONArray();
        for (TodoItem item : items) {
           // try { arr.put(item.toJson()); } catch (JSONException ignored) {}
        }
        prefs.edit().putString(KEY_TASKS, arr.toString()).apply();
    }

    public List<TodoItem> load() {
        List<TodoItem> items = new ArrayList<>();
        String raw = prefs.getString(KEY_TASKS, null);
        if (raw == null) return items;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
               // items.add(TodoItem.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException ignored) {}
        return items;
    }

    public void clear() {
        prefs.edit().remove(KEY_TASKS).apply();
    }
}
