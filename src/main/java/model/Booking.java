package model;

import java.time.LocalDateTime;

public class Booking {

    private String studentId;
    private int slotId;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;

    public Booking(String studentId, int slotId) {
        this.studentId = studentId;
        this.slotId = slotId;
        this.status = "ACTIVE";
        this.paymentStatus = "UNPAID";
    }

    public String getStudentId() {
        return studentId;
    }

    public int getSlotId() {
        return slotId;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
