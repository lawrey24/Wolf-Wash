import java.sql.Date;
import java.sql.Time;

public class Booking{
    private final int slotID;
    private final int machineID;
    private final int dryerID;
    private final Date slotDate;
    private final Time startTime;
    private final Time endTime;

    public Booking(int slotID, int machineID, int dryerID, Date slotDate, Time startTime,Time endTime ){
        this.slotID = slotID;
        this.machineID  = machineID;
        this.dryerID = dryerID;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSlotID(){return slotID;}
    public int getMachineID(){return machineID;}
    public int getDryerID(){return dryerID;}
    public Date getSlotDate(){return slotDate;}
    public Time getStartTime(){return startTime;}
    public Time getEndTime(){return endTime;}

    @Override
    public String toString(){
        return String.format("Slot# :%d | Washer: %d | Dryer: %d | %s(%s-%s)",
               slotID, machineID, dryerID, slotDate, startTime, endTime); 
    }
}
