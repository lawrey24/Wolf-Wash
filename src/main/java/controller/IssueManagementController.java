package controller;
import service.IssueManagementService;import model.ReportedIssue;import model.IssueUpdate;import java.sql.SQLException;import java.util.List;
public class IssueManagementController {
 private final IssueManagementService service=new IssueManagementService();private String adminId;
 public boolean login(String id,String password)throws SQLException{adminId=null;if(service.login(id,password)){adminId=id.trim();return true;}return false;}
 public void logout(){adminId=null;}private void requireLogin(){if(adminId==null)throw new IllegalStateException("Please log in as an administrator.");}
 public String getAdminId(){return adminId;}public List<ReportedIssue> list(String filter)throws SQLException{requireLogin();return service.list(filter);}public ReportedIssue get(int id)throws SQLException{requireLogin();return service.get(id);}public List<IssueUpdate> history(int id)throws SQLException{requireLogin();return service.history(id);}public List<String[]> notifications()throws SQLException{requireLogin();return service.notifications();}
 public void changeStatus(int id,String status,String note)throws SQLException{requireLogin();service.changeStatus(id,adminId,status,note);}public void addNote(int id,String note)throws SQLException{requireLogin();service.addNote(id,adminId,note);}
}
