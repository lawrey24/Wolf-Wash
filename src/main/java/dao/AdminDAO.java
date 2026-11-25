package dao;
import java.sql.*;
public class AdminDAO {
 public boolean authenticate(String adminId,String password)throws SQLException{
  String sql="SELECT admin_id FROM admins WHERE admin_id=? AND password_hash=? AND role IN ('ADMIN','MAINTENANCE')";
  try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,adminId);p.setString(2,password);try(ResultSet r=p.executeQuery()){return r.next();}}
 }
}
