package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Machine;

public class MachineDAO {

    public List<Machine> getAllMachinesAndDryers(Connection conn)
            throws SQLException {

        List<Machine> list = new ArrayList<>();

        String sql = """
                SELECT machine_id AS id,
                       'WASHER' AS machine_type,
                       machine_label AS label,
                       status,
                       out_of_service_reason,
                       updated_by,
                       updated_at
                FROM machines
                UNION ALL
                SELECT dryer_id AS id,
                       'DRYER' AS machine_type,
                       dryer_label AS label,
                       status,
                       out_of_service_reason,
                       updated_by,
                       updated_at
                FROM dryers
                ORDER BY machine_type, id
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Machine(
                        rs.getInt("id"),
                        rs.getString("machine_type"),
                        rs.getString("label"),
                        rs.getString("status"),
                        rs.getString("out_of_service_reason"),
                        rs.getString("updated_by"),
                        rs.getTimestamp("updated_at")
                ));
            }
        }

        return list;
    }

    public boolean updateMachineStatus(
            Connection conn,
            String machineType,
            int id,
            String status,
            String reason,
            String updatedBy) throws SQLException {

        String sql = "DRYER".equalsIgnoreCase(machineType)
                ? """
                  UPDATE dryers
                  SET status = ?,
                      out_of_service_reason = ?,
                      updated_by = ?,
                      updated_at = CURRENT_TIMESTAMP
                  WHERE dryer_id = ?
                  """
                : """
                  UPDATE machines
                  SET status = ?,
                      out_of_service_reason = ?,
                      updated_by = ?,
                      updated_at = CURRENT_TIMESTAMP
                  WHERE machine_id = ?
                  """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, reason);
            stmt.setString(3, updatedBy);
            stmt.setInt(4, id);
            return stmt.executeUpdate() == 1;
        }
    }
}
