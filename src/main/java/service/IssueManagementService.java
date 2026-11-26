package service;
import dao.AdminDAO;import dao.ReportedIssueDAO;import model.ReportedIssue;import model.IssueUpdate;import java.sql.SQLException;import java.util.List;
public class IssueManagementService {
 private final AdminDAO admins=new AdminDAO();private final ReportedIssueDAO issues=new ReportedIssueDAO();
 public boolean login(String id,String password)throws SQLException{return id!=null&&!id.isBlank()&&password!=null&&!password.isBlank()&&admins.authenticate(id.trim(),password);}
 public List<ReportedIssue> list(String filter)throws SQLException{return filter.equals("ALL")?issues.getAll():issues.getByStatus(filter);}
 public ReportedIssue get(int id)throws SQLException{return issues.getById(id);}
 public List<IssueUpdate> history(int id)throws SQLException{return issues.history(id);}
 public List<String[]> notifications()throws SQLException{return issues.notifications();}
    public void changeStatus(int id, String admin, String status, String note) throws SQLException {
        if (!List.of("PENDING", "IN_PROGRESS", "RESOLVED").contains(status)) {
            throw new IllegalArgumentException("Choose Pending, In Progress, or Resolved.");
        }
        issues.addUpdate(id, admin, status, cleanNote(note));
    }

    public void addNote(int id, String admin, String note) throws SQLException {
        String cleaned = cleanNote(note);
        if (cleaned.isEmpty()) throw new IllegalArgumentException("Progress note cannot be blank.");
        issues.addUpdate(id, admin, null, cleaned);
    }

    private String cleanNote(String note) {
        String cleaned = note == null ? "" : note.strip();
        if (cleaned.length() > 1000) throw new IllegalArgumentException("Use at most 1000 characters for a note.");
        return cleaned;
    }
}
