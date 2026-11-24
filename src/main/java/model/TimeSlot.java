package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlot {

    private int slotId;
    private int machineId;
    private int dryerId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;

    public TimeSlot(int slotId, int machineId,
                    int dryerId, LocalDate slotDate,
                    LocalTime startTime, LocalTime endTime,
                    String status) {

        this.slotId = slotId;
        this.machineId = machineId;
        this.dryerId = dryerId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getMachineId() {
        return machineId;
    }

    public int getDryerId() {
        return dryerId;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

     public void setStatus(String status) {
        this.status = status;
    }

    @Override
public String toString() {
    return "Slot ID: " + slotId
            + " | Machine ID: " + machineId
            + " | Dryer ID: " + dryerId
            + " | Date: " + slotDate
            + " | Start: " + startTime
            + " | End: " + endTime
            + " | Status: " + status;
}
}

