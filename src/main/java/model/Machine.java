package model;

import java.sql.Timestamp;

public class Machine {

    private int id;
    private String machineType; // "WASHER" or "DRYER"
    private String label;
    private String status;      // e.g., "AVAILABLE","OUT_OF_SERVICE"
    private String outOfServiceReason;
    private String updatedBy;
    private Timestamp updatedAt;

    public Machine(int id, String machineType, String label, String status,
                   String outOfServiceReason, String updatedBy, Timestamp updatedAt) {
        this.id = id;
        this.machineType = machineType;
        this.label = label;
        this.status = status;
        this.outOfServiceReason = outOfServiceReason;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getMachineType() {
        return machineType;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public String getOutOfServiceReason() {
        return outOfServiceReason;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        String base = machineType + " #" + id
                + (label != null && !label.isBlank() ? " (" + label + ")" : "")
                + " | Status: " + status;

        if ("OUT_OF_SERVICE".equalsIgnoreCase(status)
                && outOfServiceReason != null && !outOfServiceReason.isBlank()) {
            base += " | Reason: " + outOfServiceReason;
        }

        if (updatedBy != null && !updatedBy.isBlank()) {
            base += " | Updated By: " + updatedBy;
        }

        return base;
    }
}
