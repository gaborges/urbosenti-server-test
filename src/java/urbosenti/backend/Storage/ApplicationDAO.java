/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.Storage;

import java.sql.Connection;
import java.sql.ResultSet;
import urbosenti.backend.service.model.SensingModule;
import urbosenti.backend.util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import urbosenti.backend.service.model.DeviceSetup;

/**
 *
 * @author Guilherme
 */
public class ApplicationDAO {

    private DBConnection dbc;
    private Connection connection;

    public ApplicationDAO(DBConnection dbc) {
        this.dbc = dbc;
        connection = dbc.getConnection();
    }

    public void insertSensingModule(SensingModule sensingModule) throws SQLException {

        String sql = "INSERT INTO applications (application_uid,password,expiration_date,service_id)"
                + " VALUES(?,?,?,?); ";
        PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, sensingModule.getUid());
        stmt.setString(2, sensingModule.getPassword());
        stmt.setLong(3, (sensingModule.getExpirationTime() != null) ? sensingModule.getExpirationTime().getTime() : 0);
        stmt.setInt(4, sensingModule.getBackendService().getId());
        stmt.execute();
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                sensingModule.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
        stmt.close();
    }

    public void insertSensingModuleSetups(SensingModule sensingModule) throws SQLException {
        for (DeviceSetup ds : sensingModule.getDeviceSetups()) {
            String sql = "INSERT INTO device_setups (setup_id,application_id,content)"
                    + " VALUES(?,?,?); ";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setInt(1, ds.getSetupId());
            stmt.setInt(2, sensingModule.getId());
            stmt.setString(3, ds.getContent());
            stmt.executeUpdate();
            stmt.close();
        }
    }

    public boolean sensingModuleIsRegistered(String applicationUid) throws SQLException {
        String sql = "SELECT application_uid FROM applications WHERE application_uid = ? ;";
        PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, applicationUid);
        ResultSet rs;
        rs = stmt.executeQuery();
        if (rs.next()) {
            rs.close();
            stmt.close();
            return true;
        }
        stmt.close();
        return false;
    }

    public void updateSubscription(String applicationUid) throws SQLException {
        String sql = "UPDATE applications SET has_subscribed_upload_rate = ? WHERE application_uid = ? ;";
        PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        stmt.setString(2, applicationUid);
        stmt.executeUpdate();
        stmt.close();
    }

    public Double hasUploadRateToSend(String applicationUid) throws SQLException {
        String sql = "SELECT upload_rate, sent FROM application_upload_rate, applications WHERE application_id = applications.id AND application_uid = ? LIMIT 1 ;";
        PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, applicationUid);
        ResultSet rs;
        rs = stmt.executeQuery();
        if (rs.next()) {
            if(rs.getBoolean("sent")){
                return null;
            } else {
                return rs.getDouble("upload_rate");
            }
        }
        stmt.close();
        return null;
    }

    public void updateLastUploadRateAsSent(int applicationId) throws SQLException {
        String sql = "UPDATE application_upload_rate SET sent = ? WHERE application_id = ? ;";
        PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        stmt.setInt(2, applicationId);
        stmt.executeUpdate();
        stmt.close();
    }

    public int getApplicationIDByUID(String applicationUID) throws SQLException {
        String sql;
        PreparedStatement stmt;
        ResultSet rs;
        // busca o ID no sistema
        sql = "SELECT id FROM applications WHERE application_uid = ? ;";
        stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, applicationUID);
        rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("id");
    }

    public int insertSensinModuleErrorReport(Object jsonContent, int applicationID) throws SQLException {
        Integer i = 0;
        String sql = "INSERT INTO system_reports (application_id,content)"
                + " VALUES(?,?); ";
        PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, applicationID);
        stmt.setString(2, jsonContent.toString());
        stmt.execute();
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                i = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
        stmt.close();
        return i;
    }
}
