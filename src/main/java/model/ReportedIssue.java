package model;

import java.time.LocalDateTime;

public class ReportedIssue {
    private final int issueId;
    private final String studentId, studentName, title, description, status;
    private final LocalDateTime createdAt, updatedAt;
    public ReportedIssue(int issueId, String studentId, String studentName, String title,
                         String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.issueId=issueId; this.studentId=studentId; this.studentName=studentName;
        this.title=title; this.description=description; this.status=status;
        this.createdAt=createdAt; this.updatedAt=updatedAt;
    }
    public int getIssueId(){return issueId;} public String getStudentId(){return studentId;}
    public String getStudentName(){return studentName;} public String getTitle(){return title;}
    public String getDescription(){return description;} public String getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
    @Override public String toString(){return "#"+issueId+" | "+title+" | "+status+" | Student: "+studentName+" ("+studentId+") | "+createdAt+"\n  "+description;}
}
