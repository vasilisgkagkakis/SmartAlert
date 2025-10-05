package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.HomepageViewModel;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle toggle;
    protected FloatingActionButton fab;
    private boolean useDrawer = true;
    private HomepageViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomepageViewModel.class);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (useDrawer && drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    BaseActivity.super.onBackPressed();
                }
            }
        });
    }

    // For activities WITHOUT drawer (like MainActivity)
    protected void setContentViewWithoutDrawer(int layoutResId) {
        useDrawer = false;
        super.setContentView(layoutResId);
    }

    // For activities WITH drawer
    protected void setContentViewWithDrawer(int layoutResId) {
        useDrawer = true;
        super.setContentView(R.layout.activity_base_with_drawer);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResId, contentFrame, true);
        setupNavigationDrawer();
        setupFAB();
    }

    // Rest of your existing methods remain the same...
    protected void setupNavigationDrawer() {
        if (!useDrawer) return;

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    protected void setupFAB() {
        fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> onFabClick());
        }
    }

    protected void onFabClick() {
        // Default implementation - can be overridden
    }

    protected void setFabVisible() {
        if (fab != null) {
            fab.setVisibility(true ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (useDrawer && drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);

            // Handle navigation clicks
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                viewModel.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                viewModel.logout();
                if (!(this instanceof HomepageActivity)) {
                    startActivity(new Intent(this, HomepageActivity.class));
                    finish();
                }
                return true;
            }
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (useDrawer && toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
