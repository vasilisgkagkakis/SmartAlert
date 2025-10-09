package com.unipi.gkagkakis.smartalert.model;

import java.util.ArrayList;
import java.util.List;

public class AlertGroup {
    private List<Alert> alerts;
    private String groupLocation;
    private int alertCount;
    private boolean isExpanded;

    public AlertGroup() {
        this.alerts = new ArrayList<>();
        this.isExpanded = false;
    }

    public AlertGroup(List<Alert> alerts, String groupLocation) {
        this.alerts = alerts != null ? alerts : new ArrayList<>();
        this.groupLocation = groupLocation;
        this.alertCount = this.alerts.size();
        this.isExpanded = false;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
        this.alertCount = alerts != null ? alerts.size() : 0;
    }

    public void addAlert(Alert alert) {
        if (this.alerts == null) {
            this.alerts = new ArrayList<>();
        }
        this.alerts.add(alert);
        this.alertCount = this.alerts.size();
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

    public Alert getFirstAlert() {
        return alerts != null && !alerts.isEmpty() ? alerts.get(0) : null;
    }
}
