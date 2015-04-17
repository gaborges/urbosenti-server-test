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
    
    public void insertSensingModule(SensingModule sensingModule) throws SQLException{
        
        String sql = "INSERT INTO applications (application_uid,password,expiration_date,service_id)"
                + " VALUES(?,?,?,?); ";
        PreparedStatement stmt = this.connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, sensingModule.getUid());
        stmt.setString(2, sensingModule.getPassword());
        stmt.setLong(3, (sensingModule.getExpirationTime()!=null)?sensingModule.getExpirationTime().getTime():0);
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
    
    public void insertSensingModuleSetups(SensingModule sensingModule) throws SQLException{
        for(DeviceSetup ds:sensingModule.getDeviceSetups()){
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
}
