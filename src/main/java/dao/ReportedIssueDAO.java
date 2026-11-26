package dao;
import model.ReportedIssue; import model.IssueUpdate; import java.sql.*; import java.util.*;
public class ReportedIssueDAO {
 public List<ReportedIssue> getAll()throws SQLException{String sql="SELECT i.issue_id,i.student_id,s.full_name,i.title,i.description,i.status,i.created_at,i.updated_at FROM reported_issues i JOIN students s ON s.student_id=i.student_id ORDER BY i.created_at DESC"; List<ReportedIssue> out=new ArrayList<>(); try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return out;}
 public List<ReportedIssue> getByStatus(String status)throws SQLException{String sql="SELECT i.issue_id,i.student_id,s.full_name,i.title,i.description,i.status,i.created_at,i.updated_at FROM reported_issues i JOIN students s ON s.student_id=i.student_id WHERE i.status=? ORDER BY i.created_at DESC";List<ReportedIssue> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,status);try(ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}}return out;}
 private ReportedIssue map(ResultSet r)throws SQLException{return new ReportedIssue(r.getInt("issue_id"),r.getString("student_id"),r.getString("full_name"),r.getString("title"),r.getString("description"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime(),r.getTimestamp("updated_at").toLocalDateTime());}
 public ReportedIssue getById(int id)throws SQLException{String sql="SELECT i.issue_id,i.student_id,s.full_name,i.title,i.description,i.status,i.created_at,i.updated_at FROM reported_issues i JOIN students s ON s.student_id=i.student_id WHERE i.issue_id=?";try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}}
    public void create(String student, String title, String description) throws SQLException {
        String sql = "INSERT INTO reported_issues (student_id, title, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.executeUpdate();
        }
    }

    public List<ReportedIssue> forStudent(String student) throws SQLException {
        String sql = """
                SELECT i.*, s.full_name FROM reported_issues i
                JOIN students s ON s.student_id = i.student_id
                WHERE i.student_id = ? ORDER BY i.issue_id DESC
                """;
        List<ReportedIssue> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student);
            try (ResultSet rows = stmt.executeQuery()) {
                while (rows.next()) result.add(map(rows));
            }
        }
        return result;
    }

    public List<String> alertsForStudent(String student) throws SQLException {
        String sql = "SELECT message FROM issue_notifications WHERE student_id = ? ORDER BY notification_id DESC";
        List<String> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student);
            try (ResultSet rows = stmt.executeQuery()) {
                while (rows.next()) result.add(rows.getString(1));
            }
        }
        return result;
    }

    public void addUpdate(int id, String admin, String status, String note) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String previous;
                String student;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT status, student_id FROM reported_issues WHERE issue_id = ? FOR UPDATE")) {
                    stmt.setInt(1, id);
                    try (ResultSet row = stmt.executeQuery()) {
                        if (!row.next()) throw new IllegalArgumentException("Issue not found.");
                        previous = row.getString("status");
                        student = row.getString("student_id");
                    }
                }

                boolean changed = status != null && !status.equals(previous);
                if (!changed && note.isBlank()) {
                    throw new IllegalArgumentException("Choose a different status or enter a progress note.");
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE reported_issues SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE issue_id = ?")) {
                    stmt.setString(1, changed ? status : previous);
                    stmt.setInt(2, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO issue_updates (issue_id, admin_id, old_status, new_status, note) VALUES (?, ?, ?, ?, ?)")) {
                    stmt.setInt(1, id);
                    stmt.setString(2, admin);
                    stmt.setString(3, changed ? previous : null);
                    stmt.setString(4, changed ? status : null);
                    stmt.setString(5, note);
                    stmt.executeUpdate();
                }

                String message = "Issue #" + id + (changed
                        ? " status changed to " + ("PENDING".equals(status) ? "new" : status.replace('_', ' ').toLowerCase(Locale.ROOT)) : " has a new progress note");
                if (!note.isBlank()) {
                    int limit = Math.min(note.codePointCount(0, note.length()), 300);
                    message += ". " + note.substring(0, note.offsetByCodePoints(0, limit));
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO issue_notifications (issue_id, student_id, message) VALUES (?, ?, ?)")) {
                    stmt.setInt(1, id);
                    stmt.setString(2, student);
                    stmt.setString(3, message);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

 public List<IssueUpdate> history(int issueId)throws SQLException{String sql="SELECT update_id,issue_id,admin_id,old_status,new_status,note,created_at FROM issue_updates WHERE issue_id=? ORDER BY update_id";List<IssueUpdate> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,issueId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new IssueUpdate(r.getInt(1),r.getInt(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getTimestamp(7).toLocalDateTime()));}}return out;}
 public List<String[]> notifications()throws SQLException{String sql="SELECT notification_id,issue_id,student_id,message,created_at,delivery_status FROM issue_notifications ORDER BY created_at DESC";List<String[]> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}return out;}
}
