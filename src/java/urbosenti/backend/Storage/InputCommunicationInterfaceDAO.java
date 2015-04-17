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
import urbosenti.backend.service.model.DeviceInputCommunicationInterface;
import urbosenti.backend.service.model.DeviceSetup;
import urbosenti.backend.service.model.ExtraParameter;
import urbosenti.backend.service.model.SensingModule;
import urbosenti.backend.util.DBConnection;

/**
 *
 * @author Guilherme
 */
public class InputCommunicationInterfaceDAO {
    
    private final DBConnection dbc;
    private final Connection connection;

    public InputCommunicationInterfaceDAO(DBConnection dbc) {
        this.dbc = dbc;
        connection = dbc.getConnection();
    }
    
    public void insertInputCommunicationExtraParameters(SensingModule sensingModule) throws SQLException{
        
        for(DeviceInputCommunicationInterface communicationInterface:sensingModule.getInputCommunicationInterfaces()){
            for(ExtraParameter ep : communicationInterface.getExtraParameters()){
                String sql = "INSERT INTO device_input_communication_parameter_content (parameter_id,application_id,content)"
                        + " VALUES(?,?,?); ";
                PreparedStatement stmt = this.connection.prepareStatement(sql);
                stmt.setInt(1, ep.getParameterId() );
                stmt.setInt(2, sensingModule.getId());
                stmt.setString(3, ep.getContent());
                stmt.executeUpdate();
                stmt.close();
            }
        }
    }
}
