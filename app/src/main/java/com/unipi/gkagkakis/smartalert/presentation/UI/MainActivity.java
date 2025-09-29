package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.hideStatusBar(this);
        initViews();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}