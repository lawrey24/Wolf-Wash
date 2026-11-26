package model;
import java.time.LocalDateTime;
public class IssueUpdate {
    private final int updateId, issueId; private final String adminId, oldStatus, newStatus, note; private final LocalDateTime createdAt;
    public IssueUpdate(int updateId,int issueId,String adminId,String oldStatus,String newStatus,String note,LocalDateTime createdAt){this.updateId=updateId;this.issueId=issueId;this.adminId=adminId;this.oldStatus=oldStatus;this.newStatus=newStatus;this.note=note;this.createdAt=createdAt;}
    public int getUpdateId(){return updateId;} public int getIssueId(){return issueId;} public String getAdminId(){return adminId;} public String getOldStatus(){return oldStatus;} public String getNewStatus(){return newStatus;} public String getNote(){return note;} public LocalDateTime getCreatedAt(){return createdAt;}
    @Override public String toString(){return createdAt+" | "+adminId+" | "+(oldStatus==null?"NOTE":oldStatus+" -> "+newStatus)+" | "+note;}
}
