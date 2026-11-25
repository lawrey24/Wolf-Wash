
package model;

import java.sql.Date;
import java.sql.Time;

public class Booking {

    private String studentId;
    private int slotID;
    private int machineID;
    private int dryerID;
    private Date slotDate;
    private Time startTime;
    private Time endTime;

    // Original constructor - keeps compatibility with existing code
    public Booking(int slotID, int machineID, int dryerID,
                   Date slotDate, Time startTime, Time endTime) {

        this.slotID = slotID;
        this.machineID = machineID;
        this.dryerID = dryerID;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Constructor that also includes the student's ID
    public Booking(String studentId, int slotID, int machineID,
                   int dryerID, Date slotDate, Time startTime, Time endTime) {

        this.studentId = studentId;
        this.slotID = slotID;
        this.machineID = machineID;
        this.dryerID = dryerID;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Booking(String studentId, int slotID) {
    this.studentId = studentId;
    this.slotID = slotID;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getSlotID() {
        return slotID;
    }

    public int getSlotId() {
        return slotID;
    }

    public void setSlotID(int slotID) {
        this.slotID = slotID;
    }

    public int getMachineID() {
        return machineID;
    }

    public int getDryerID() {
        return dryerID;
    }

    public Date getSlotDate() {
        return slotDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }
    private String paymentStatus;
    public String getPaymentStatus() { return paymentStatus; }

    public void setPaymentStatus(String status) { paymentStatus = status; }

    @Override
public String toString() {
    return "Student ID: " + studentId
            + " | Slot ID: " + slotID
            + " | Machine ID: " + machineID
            + " | Dryer ID: " + dryerID
            + " | Date: " + slotDate
            + " | Start Time: " + startTime
            + " | End Time: " + endTime
            + (paymentStatus == null ? "" : " | Payment status: " + paymentStatus);
}
}
