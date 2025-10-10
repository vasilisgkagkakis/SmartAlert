package com.unipi.gkagkakis.smartalert.model;

import java.util.ArrayList;
import java.util.List;

public class SubmittedAlertGroup {
    private List<SubmittedAlert> submittedAlerts;
    private String groupLocation;
    private int alertCount;
    private boolean isExpanded;
    private final String status; // "PENDING", "ACCEPTED", "REJECTED"

    public SubmittedAlertGroup() {
        this.submittedAlerts = new ArrayList<>();
        this.isExpanded = false;
        this.status = "PENDING";
    }

    public List<SubmittedAlert> getSubmittedAlerts() {
        return submittedAlerts;
    }

    public void addSubmittedAlert(SubmittedAlert submittedAlert) {
        if (this.submittedAlerts == null) {
            this.submittedAlerts = new ArrayList<>();
        }
        this.submittedAlerts.add(submittedAlert);
        this.alertCount = this.submittedAlerts.size();
    }

    public String getGroupLocation() {
        return groupLocation;
    }

    public void setGroupLocation(String groupLocation) {
        this.groupLocation = groupLocation;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getStatus() {
        return status;
    }

    public SubmittedAlert getFirstAlert() {
        if (submittedAlerts != null && !submittedAlerts.isEmpty()) {
            return submittedAlerts.get(0);
        }
        return null;
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }
}
