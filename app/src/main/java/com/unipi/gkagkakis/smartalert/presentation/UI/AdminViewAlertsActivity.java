package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.CoordinatesUtil;
import com.unipi.gkagkakis.smartalert.Utils.LocationUtils;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.data.repository.AlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.data.repository.SubmittedAlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.data.service.AlertSeedService;
import com.unipi.gkagkakis.smartalert.domain.repository.AlertRepository;
import com.unipi.gkagkakis.smartalert.domain.repository.SubmittedAlertRepository;
import com.unipi.gkagkakis.smartalert.model.Alert;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlertGroup;
import com.unipi.gkagkakis.smartalert.presentation.adapter.SubmittedAlertGroupAdapter;
import com.unipi.gkagkakis.smartalert.service.FCMNotificationSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminViewAlertsActivity extends BaseActivity implements SubmittedAlertGroupAdapter.OnGroupActionListener {

    private static final double GROUPING_DISTANCE_KM = 5.0;

    private SubmittedAlertGroupAdapter adapter;
    private SubmittedAlertRepository submittedAlertRepository;
    private AlertRepository alertRepository;
    private List<SubmittedAlertGroup> submittedAlertGroups;
    private FCMNotificationSender fcmNotificationSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_admin_view_alerts);
        StatusBarHelper.hideStatusBar(this);

        initViews();
        initRepositories();
        loadSubmittedAlerts();
    }

    private void initViews() {
        // Set up the toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAlerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        submittedAlertGroups = new ArrayList<>();
        adapter = new SubmittedAlertGroupAdapter(submittedAlertGroups, this);
        recyclerView.setAdapter(adapter);
    }

    private void initRepositories() {
        submittedAlertRepository = SubmittedAlertRepositoryImpl.getInstance();
        alertRepository = AlertRepositoryImpl.getInstance();
        fcmNotificationSender = new FCMNotificationSender(this);
    }

    private void loadSubmittedAlerts() {
        submittedAlertRepository.getAllSubmittedAlerts(new SubmittedAlertRepository.GetAllSubmittedAlertsCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(@NonNull List<SubmittedAlert> submittedAlerts) {
                groupSubmittedAlerts(submittedAlerts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Exception e) {
                Toast.makeText(AdminViewAlertsActivity.this,
                        "Failed to load submitted alerts: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void groupSubmittedAlerts(List<SubmittedAlert> submittedAlerts) {
        submittedAlertGroups.clear();
        List<SubmittedAlert> remainingAlerts = new ArrayList<>(submittedAlerts);

        while (!remainingAlerts.isEmpty()) {
            SubmittedAlert currentAlert = remainingAlerts.remove(0);
            SubmittedAlertGroup group = new SubmittedAlertGroup();
            group.addSubmittedAlert(currentAlert);

            // Find alerts within 5km of the current alert
            List<SubmittedAlert> alertsToRemove = new ArrayList<>();
            for (SubmittedAlert otherAlert : remainingAlerts) {
                if (areAlertsNearby(currentAlert, otherAlert)) {
                    group.addSubmittedAlert(otherAlert);
                    alertsToRemove.add(otherAlert);
                }
            }

            // Remove grouped alerts from remaining alerts
            remainingAlerts.removeAll(alertsToRemove);

            // Set group location based on first alert and count
            if (group.getAlertCount() > 1) {
                group.setGroupLocation(currentAlert.getLocation() + " (" + group.getAlertCount() + " nearby alerts)");
            } else {
                group.setGroupLocation(currentAlert.getLocation());
            }

            submittedAlertGroups.add(group);
        }

        // Sort groups by creation time (most recent first)
        submittedAlertGroups.sort((group1, group2) -> {
            SubmittedAlert alert1 = group1.getFirstAlert();
            SubmittedAlert alert2 = group2.getFirstAlert();
            if (alert1 != null && alert2 != null && alert1.getCreatedAt() != null && alert2.getCreatedAt() != null) {
                return alert2.getCreatedAt().compareTo(alert1.getCreatedAt());
            }
            return 0;
        });
    }

    private boolean areAlertsNearby(SubmittedAlert alert1, SubmittedAlert alert2) {
        String location1 = alert1.getLocation();
        String location2 = alert2.getLocation();

        if (location1 == null || location2 == null) {
            return false;
        }

        // Try to parse coordinates from location strings
        String coords1 = CoordinatesUtil.tryParseCoordinates(location1);
        String coords2 = CoordinatesUtil.tryParseCoordinates(location2);

        if (coords1 == null || coords2 == null) {
            // If we can't parse coordinates, fall back to string comparison
            return location1.equalsIgnoreCase(location2);
        }

        double distance = CoordinatesUtil.calculateDistanceFromStrings(coords1, coords2);
        return distance >= 0 && distance <= GROUPING_DISTANCE_KM;
    }

    @Override
    public void onAcceptGroup(SubmittedAlertGroup group, int position) {
        // Create an alert from the submitted alert group
        SubmittedAlert firstAlert = group.getFirstAlert();
        if (firstAlert != null) {
            Alert newAlert = new Alert(
                    null, // ID will be generated by Firestore
                    firstAlert.getType(),
                    firstAlert.getSeverity(),
                    firstAlert.getLocation(),
                    createGroupDescription(group),
                    firstAlert.getImageUrl(),
                    firstAlert.getUserId(),
                    null // createdAt will be set by Firestore
            );

            alertRepository.createAlert(newAlert, new AlertRepository.CreateAlertCallback() {
                @Override
                public void onSuccess(@NonNull String alertId) {
                    // Delete all submitted alerts in the group
                    deleteSubmittedAlertsInGroup(group, position);

                    sendPushNotificationToAllUsers(newAlert);
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Toast.makeText(AdminViewAlertsActivity.this,
                            "Failed to create alert: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRejectGroup(SubmittedAlertGroup group, int position) {
        // Simply delete all submitted alerts in the group
        deleteSubmittedAlertsInGroup(group, position);
        Toast.makeText(this, "Alert group rejected", Toast.LENGTH_SHORT).show();
    }

    private void deleteSubmittedAlertsInGroup(SubmittedAlertGroup group, int position) {
        List<SubmittedAlert> alertsToDelete = new ArrayList<>(group.getSubmittedAlerts());
        final int[] deletedCount = {0};
        final int totalCount = alertsToDelete.size();

        for (SubmittedAlert alert : alertsToDelete) {
            submittedAlertRepository.deleteSubmittedAlert(alert.getId(), new SubmittedAlertRepository.DeleteSubmittedAlertCallback() {
                @Override
                public void onSuccess() {
                    deletedCount[0]++;
                    if (deletedCount[0] == totalCount) {
                        // All alerts deleted, remove group from list
                        submittedAlertGroups.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Toast.makeText(AdminViewAlertsActivity.this,
                            "Failed to delete some alerts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("DefaultLocale")
    private String createGroupDescription(SubmittedAlertGroup group) {
        if (group.getAlertCount() == 1) {
            return group.getFirstAlert().getDescription();
        } else {
            return String.format("Multiple reports (%d alerts) of %s in the area. %s",
                    group.getAlertCount(),
                    group.getFirstAlert().getType().toLowerCase(),
                    group.getFirstAlert().getDescription());
        }
    }

    private void sendPushNotificationToAllUsers(Alert alert) {
        // Parse coordinates from the alert location
        String coordinates = CoordinatesUtil.tryParseCoordinates(alert.getLocation());
        if (coordinates == null) {
            Toast.makeText(this, "Cannot send notifications: Invalid location coordinates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract latitude and longitude
        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            Toast.makeText(this, "Cannot send notifications: Invalid coordinate format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());

            // Convert coordinates to readable location name
            LocationUtils.getAddressFromCoordinates(this, latitude, longitude, new LocationUtils.GeocodeCallback() {
                @Override
                public void onSuccess(String address) {
                    // Use the readable address for notifications
                    fcmNotificationSender.sendAlertNotificationToNearbyUsers(
                            latitude,
                            longitude,
                            alert.getType(),
                            alert.getDescription(),
                            address, // Use readable address instead of coordinates
                            alert.getSeverity() // Include severity parameter
                    );

                    Toast.makeText(AdminViewAlertsActivity.this, "Notifications sent to nearby users (within 10km)", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Log.e("AdminViewAlerts", "Failed to get address: " + error);
                    // Fallback to coordinates if geocoding fails
                    fcmNotificationSender.sendAlertNotificationToNearbyUsers(
                            latitude,
                            longitude,
                            alert.getType(),
                            alert.getDescription(),
                            "nearby location", // Generic fallback
                            alert.getSeverity() // Include severity parameter
                    );

                    Toast.makeText(AdminViewAlertsActivity.this, "Notifications sent to nearby users (within 10km)", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cannot send notifications: Invalid coordinate values", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_view_alerts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_seed_data) {
            seedAlertData();
            return true;
        } else if (id == R.id.action_refresh) {
            loadSubmittedAlerts();
            Toast.makeText(this, "Refreshing alerts...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void seedAlertData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to seed data", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        AlertSeedService seedService = new AlertSeedService();

        // Show a loading message
        Toast.makeText(this, "Seeding alert data, please wait...", Toast.LENGTH_SHORT).show();

        seedService.seedTestAlerts(userId, new AlertSeedService.SeedCallback() {
            @Override
            public void onSeedComplete(int successCount, int totalCount) {
                String message = String.format(Locale.getDefault(), "Seeded %d/%d alerts successfully", successCount, totalCount);
                Toast.makeText(AdminViewAlertsActivity.this, message, Toast.LENGTH_LONG).show();

                // Refresh the submitted alerts list to show the new data
                loadSubmittedAlerts();
            }

            @Override
            public void onSeedError(@NonNull Exception e) {
                Toast.makeText(AdminViewAlertsActivity.this,
                        "Failed to seed alert data: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}