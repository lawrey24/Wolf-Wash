package service;
import dao.AdminDAO;import dao.ReportedIssueDAO;import model.ReportedIssue;import model.IssueUpdate;import java.sql.SQLException;import java.util.List;
public class IssueManagementService {
 private final AdminDAO admins=new AdminDAO();private final ReportedIssueDAO issues=new ReportedIssueDAO();
 public boolean login(String id,String password)throws SQLException{return id!=null&&!id.isBlank()&&password!=null&&!password.isBlank()&&admins.authenticate(id.trim(),password);}
 public List<ReportedIssue> list(String filter)throws SQLException{return filter.equals("ALL")?issues.getAll():issues.getByStatus(filter);}
 public ReportedIssue get(int id)throws SQLException{return issues.getById(id);}
 public List<IssueUpdate> history(int id)throws SQLException{return issues.history(id);}
 public List<String[]> notifications()throws SQLException{return issues.notifications();}
 public void changeStatus(int issueId,String adminId,String status,String note)throws SQLException{if(!List.of("PENDING","IN_PROGRESS","RESOLVED").contains(status))throw new IllegalArgumentException("Choose Pending, In Progress, or Resolved.");if(note==null)note="";ReportedIssue issue=issues.getById(issueId);if(issue==null)throw new IllegalArgumentException("Issue not found.");if(issue.getStatus().equals(status))throw new IllegalArgumentException("Issue already has that status.");issues.addUpdate(issueId,adminId,issue.getStatus(),status,note.trim());}
 public void addNote(int issueId,String adminId,String note)throws SQLException{if(note==null||note.trim().isEmpty())throw new IllegalArgumentException("Progress note cannot be blank.");if(issues.getById(issueId)==null)throw new IllegalArgumentException("Issue not found.");issues.addNote(issueId,adminId,note.trim());}
}
