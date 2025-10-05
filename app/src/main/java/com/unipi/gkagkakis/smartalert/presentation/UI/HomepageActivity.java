package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;

public class HomepageActivity extends AppCompatActivity {

    private TextView tvUserName;
    private MaterialButton btnNewAlert;
    private TextView tvLogout;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        loadUserFullName(userId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        StatusBarHelper.hideStatusBar(this);
        initViews();
        setupClickListeners();

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void loadUserFullName(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.w("Firestore", "User doc not found id=" + userId);
                        tvUserName.setText("User");
                        return;
                    }
                    String fullName = doc.getString("fullName");
                    if (fullName == null || fullName.trim().isEmpty()) {
                        tvUserName.setText("User");
                        Log.w("Firestore", "fullName missing");
                    } else {
                        tvUserName.setText(fullName);
                        Log.d("Firestore", "Full Name: " + fullName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Fetch failed", e);
                    tvUserName.setText("User");
                });
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        btnNewAlert = findViewById(R.id.btn_new_alert);
        tvLogout = findViewById(R.id.tv_logout);
    }

    private void setupClickListeners() {
        btnNewAlert.setOnClickListener(v -> {
            NewAlertFragment fragment = new NewAlertFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        tvLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(HomepageActivity.this, LoginActivity.class));
            finish();
        });
    }
}
