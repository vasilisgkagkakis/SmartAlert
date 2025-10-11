package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

import android.view.View;
import android.widget.TextView;

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
                    // Remove this callback and let the system handle the back press
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    protected void setContentViewWithDrawer(int layoutResId) {
        useDrawer = true;
        super.setContentView(R.layout.activity_base_with_drawer);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResId, contentFrame, true);
        setupNavigationDrawer();
        setupFAB();

        // Set up user name in nav header
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvNavUser = headerView.findViewById(R.id.tv_nav_user);
            viewModel.userNameOnly.observe(this, tvNavUser::setText);
            viewModel.checkUserAndLoadName(); // Ensure name is loaded
        }
    }

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
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
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
                getOnBackPressedDispatcher().onBackPressed();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_statistics) {
//                startActivity(new Intent(this, StatisticsActivity.class));
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
