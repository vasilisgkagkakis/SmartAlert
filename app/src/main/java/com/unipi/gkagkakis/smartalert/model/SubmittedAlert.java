package com.unipi.gkagkakis.smartalert.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class SubmittedAlert {
    @Exclude
    private String id;

    private String type;
    private String severity;
    private String location;
    private String description;
    private String imageUrl;
    private String userId;

    @ServerTimestamp
    private Date createdAt;

    // No-argument constructor required by Firebase Firestore for deserialization
    public SubmittedAlert() {
        // Default constructor - Firebase will populate fields via setters
    }

    // Parameterized constructor for creating new instances
    public SubmittedAlert(String id, String type, String severity, String location, String description,
                          String imageUrl, String userId, Date createdAt) {
        this.id = id;
        this.type = type;
        this.severity = severity;
        this.location = location;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}