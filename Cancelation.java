import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Cancelation{

    public boolean authenticate(String studentID, String password){
        String sql = "Select 1 FROM students WHERE studentID = ? AND passowrd_has = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setString(1, studentID);
            stmt.setString(2,password);
            
            try (ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }

        }catch (SQLException e){
            System.err.println("Authentication Error:" + e.getMessage());
            return false;
        }
    }
    
}
