package com.utkarsh.todo;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreManager {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_TASKS = "tasks";

    private final FirebaseFirestore db;
    private final String userId;

    public FirestoreManager(String userId) {
        this.db     = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    private Query tasksQuery() {
        return db.collection(COLLECTION_USERS)
                 .document(userId)
                 .collection(COLLECTION_TASKS)
                 .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /** Real-time listener — call registration.remove() to stop. */
    public ListenerRegistration listenToTasks(EventListener<QuerySnapshot> listener) {
        return tasksQuery().addSnapshotListener(listener);
    }

    public void addTask(TodoItem item, OnSuccessListener<DocumentReference> onSuccess) {
        db.collection(COLLECTION_USERS)
          .document(userId)
          .collection(COLLECTION_TASKS)
          .add(item.toMap())
          .addOnSuccessListener(onSuccess)
          .addOnFailureListener(e -> {});
    }

    public void toggleDone(String taskId, boolean done) {
        db.collection(COLLECTION_USERS)
          .document(userId)
          .collection(COLLECTION_TASKS)
          .document(taskId)
          .update("done", done);
    }

    public void deleteTask(String taskId) {
        db.collection(COLLECTION_USERS)
          .document(userId)
          .collection(COLLECTION_TASKS)
          .document(taskId)
          .delete();
    }

    /** Save/update premium flag per user */
    public void setPremium(boolean isPremium) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("isPremium", isPremium);
        db.collection(COLLECTION_USERS)
          .document(userId)
          .set(data, com.google.firebase.firestore.SetOptions.merge());
    }
}
