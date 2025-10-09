package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.CoordinatesUtil;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.data.repository.AlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.data.repository.SubmittedAlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.AlertRepository;
import com.unipi.gkagkakis.smartalert.domain.repository.SubmittedAlertRepository;
import com.unipi.gkagkakis.smartalert.model.Alert;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlertGroup;
import com.unipi.gkagkakis.smartalert.presentation.adapter.SubmittedAlertGroupAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminViewAlertsActivity extends BaseActivity implements SubmittedAlertGroupAdapter.OnGroupActionListener {

    private static final double GROUPING_DISTANCE_KM = 5.0;

    private RecyclerView recyclerView;
    private SubmittedAlertGroupAdapter adapter;
    private SubmittedAlertRepository submittedAlertRepository;
    private AlertRepository alertRepository;
    private List<SubmittedAlertGroup> submittedAlertGroups;

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
        recyclerView = findViewById(R.id.recyclerViewAlerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        submittedAlertGroups = new ArrayList<>();
        adapter = new SubmittedAlertGroupAdapter(submittedAlertGroups, this);
        recyclerView.setAdapter(adapter);

        // Set up toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Review Submitted Alerts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initRepositories() {
        submittedAlertRepository = SubmittedAlertRepositoryImpl.getInstance();
        alertRepository = AlertRepositoryImpl.getInstance();
    }

    private void loadSubmittedAlerts() {
        submittedAlertRepository.getAllSubmittedAlerts(new SubmittedAlertRepository.GetAllSubmittedAlertsCallback() {
            @Override
            public void onSuccess(List<SubmittedAlert> submittedAlerts) {
                groupSubmittedAlerts(submittedAlerts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
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

            // Set group location based on first alert
            group.setGroupLocation(currentAlert.getLocation());

            submittedAlertGroups.add(group);
        }
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
                public void onSuccess(String alertId) {
                    // Delete all submitted alerts in the group
                    deleteSubmittedAlertsInGroup(group, position);

                    // TODO: Send push notification to all users
                    sendPushNotificationToAllUsers(newAlert);

                    Toast.makeText(AdminViewAlertsActivity.this,
                        "Alert group accepted and published!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
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
                public void onError(Exception e) {
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
        // TODO: Implement push notification functionality
        // This would typically involve:
        // 1. Getting all user tokens from the database
        // 2. Creating a notification payload
        // 3. Sending via Firebase Cloud Messaging (FCM)

        // For now, just log that we would send a notification
        System.out.println("Would send push notification for alert: " + alert.getType() + " at " + alert.getLocation());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
