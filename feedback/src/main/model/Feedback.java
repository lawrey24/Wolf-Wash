package model;

import java.time.LocalDateTime;

public class Feedback {
    private final int id;
    private final String userId;
    private final String userName;
    private final String category;
    private final String message;
    private final String status;
    private final String adminNote;
    private final LocalDateTime createdAt;

    public Feedback(int id, String userId, String userName, String category,
                    String message, String status, String adminNote, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.category = category;
        this.message = message;
        this.status = status;
        this.adminNote = adminNote;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
