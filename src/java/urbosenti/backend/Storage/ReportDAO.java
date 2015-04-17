/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.Storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import urbosenti.backend.util.DBConnection;

/**
 *
 * @author Guilherme
 */
public class ReportDAO {

    private final DBConnection dbc;
    private final Connection connection;
    
    public ReportDAO(DBConnection dbc) {
        this.dbc = dbc;
        connection = dbc.getConnection();
    }
    
    public int insertGenericReport(String applicationUid,String content) throws SQLException{
        int id = 0;
        String sql = "INSERT INTO reports (content,response_time,application_uid)"
                + " VALUES(?,?,?); ";
        PreparedStatement stmt = this.connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, content.trim());
        stmt.setLong(2, 0);
        stmt.setString(3, applicationUid);
        stmt.execute();
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                id = (generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
        stmt.close();       
        return id;
    }
    
    public int insertGenericReport(String applicationUid, String content, Integer userId) throws SQLException{
        int id = 0;
        String sql = "INSERT INTO reports (content,response_time,application_uid, user_id)"
                + " VALUES(?,?,?,?); ";
        PreparedStatement stmt = this.connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, content);
        stmt.setLong(2, 0);
        stmt.setString(3, applicationUid);
        stmt.setInt(4, userId);
        stmt.execute();
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                id = (generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
        stmt.close();       
        return id;
    }
    
    public void updateReportResponseTime(long responseTime, String applicationUid) throws SQLException{
            String sql = " UPDATE reports SET response_time = ? "
                    + " WHERE id = (SELECT id FROM reports WHERE application_uid = ? ORDER BY id DESC LIMIT 1 ); ";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setLong(1, responseTime);
            stmt.setString(2, applicationUid);
            stmt.executeUpdate();
            stmt.close();
    }
}
