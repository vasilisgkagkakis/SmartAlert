package com.unipi.gkagkakis.smartalert.model;

import java.util.ArrayList;
import java.util.List;

public class SubmittedAlertGroup {
    private List<SubmittedAlert> submittedAlerts;
    private String groupLocation;
    private int alertCount;
    private boolean isExpanded;
    private String status; // "PENDING", "ACCEPTED", "REJECTED"

    public SubmittedAlertGroup() {
        this.submittedAlerts = new ArrayList<>();
        this.isExpanded = false;
        this.status = "PENDING";
    }

    public SubmittedAlertGroup(List<SubmittedAlert> submittedAlerts, String groupLocation) {
        this.submittedAlerts = submittedAlerts != null ? submittedAlerts : new ArrayList<>();
        this.groupLocation = groupLocation;
        this.alertCount = this.submittedAlerts.size();
        this.isExpanded = false;
        this.status = "PENDING";
    }

    public List<SubmittedAlert> getSubmittedAlerts() {
        return submittedAlerts;
    }

    public void setSubmittedAlerts(List<SubmittedAlert> submittedAlerts) {
        this.submittedAlerts = submittedAlerts;
        this.alertCount = submittedAlerts != null ? submittedAlerts.size() : 0;
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

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public SubmittedAlert getFirstAlert() {
        return submittedAlerts != null && !submittedAlerts.isEmpty() ? submittedAlerts.get(0) : null;
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isAccepted() {
        return "ACCEPTED".equals(status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
}
