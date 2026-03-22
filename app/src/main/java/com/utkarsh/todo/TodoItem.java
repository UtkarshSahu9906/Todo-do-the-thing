package com.utkarsh.todo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TodoItem {

    public static final String PRIORITY_HIGH   = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW    = "LOW";

    public static final String CAT_WORK     = "Work";
    public static final String CAT_PERSONAL = "Personal";
    public static final String CAT_HEALTH   = "Health";
    public static final String CAT_OTHER    = "Other";

    private String  id;
    private String  title;
    private String  category;
    private String  priority;
    private boolean done;
    private String  userId;
    private Date    createdAt;

    // Firestore requires a no-arg constructor
    public TodoItem() {}

    public TodoItem(String title, String category, String priority, String userId) {
        this.title     = title;
        this.category  = category;
        this.priority  = priority;
        this.userId    = userId;
        this.done      = false;
        this.createdAt = new Date();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("title",     title);
        m.put("category",  category);
        m.put("priority",  priority);
        m.put("done",      done);
        m.put("userId",    userId);
        m.put("createdAt", createdAt);
        return m;
    }

    public String getMoodText() {
        if (done) return "Nailed it! \uD83C\uDF89";
        switch (priority != null ? priority : PRIORITY_LOW) {
            case PRIORITY_HIGH:   return "Screaming internally \uD83D\uDD25";
            case PRIORITY_MEDIUM: return "Getting a bit nervous \uD83D\uDE05";
            default:              return "Whenever you feel like it \uD83D\uDE0E";
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public String  getId()                { return id; }
    public void    setId(String id)       { this.id = id; }
    public String  getTitle()             { return title; }
    public void    setTitle(String t)     { this.title = t; }
    public String  getCategory()          { return category; }
    public void    setCategory(String c)  { this.category = c; }
    public String  getPriority()          { return priority; }
    public void    setPriority(String p)  { this.priority = p; }
    public boolean isDone()               { return done; }
    public void    setDone(boolean d)     { this.done = d; }
    public String  getUserId()            { return userId; }
    public void    setUserId(String uid)  { this.userId = uid; }
    public Date    getCreatedAt()         { return createdAt; }
    public void    setCreatedAt(Date d)   { this.createdAt = d; }
}
