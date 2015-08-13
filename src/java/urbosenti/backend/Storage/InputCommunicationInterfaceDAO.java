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
import urbosenti.backend.communication.Address;
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

    public void insertInputCommunicationExtraParameters(SensingModule sensingModule) throws SQLException {

        for (DeviceInputCommunicationInterface communicationInterface : sensingModule.getInputCommunicationInterfaces()) {
            for (ExtraParameter ep : communicationInterface.getExtraParameters()) {
                String sql = "INSERT INTO device_input_communication_parameter_content (parameter_id,application_id,content)"
                        + " VALUES(?,?,?); ";
                PreparedStatement stmt = this.connection.prepareStatement(sql);
                stmt.setInt(1, ep.getParameterId());
                stmt.setInt(2, sensingModule.getId());
                stmt.setString(3, ep.getContent());
                stmt.executeUpdate();
                stmt.close();
            }
        }
    }

    /**
     *
     * @param applicationUid
     * @param communicationInterface
     * @param address
     */
    public Address updateAddress(String applicationUid, String communicationInterface, String address) throws SQLException  {
        String sql;
        PreparedStatement stmt;
        ResultSet rs;
        int applicationId;
        Address interfaceAddress;
        // busca o ID no sistema
        sql = "SELECT id FROM applications WHERE application_uid = ? ;";
        stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, applicationUid);
        rs = stmt.executeQuery();
        rs.next();
        applicationId = rs.getInt("id");
        rs.close();
        // verifica qual interface
        if (applicationId != 0 && communicationInterface.trim().equals(DeviceInputCommunicationInterface.SOCKET_INTERFACE)) {
            // quebrar as strings
            // {port=55666, ipv4Address=192.168.25.7}
            address = address.replace("{", "");
            address = address.replace("}", "");
            String addressPart[] = address.split(",");
            addressPart[0] = addressPart[0].replace("port=", "").trim(); // port
            addressPart[1] = addressPart[1].replace("ipv4Address=", ""); // address
            addressPart[1] = addressPart[1].trim();
            // primeiro atualiza o IP
            sql = "UPDATE device_input_communication_parameter_content SET content = ? WHERE parameter_id = ? AND application_id = ?;";
            stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, addressPart[1]);
            stmt.setInt(2, DeviceInputCommunicationInterface.SOCKET_INTERFACE_IPV4_ADDRESS_PARAMETER);
            stmt.setInt(3, applicationId);
            stmt.executeUpdate();
            stmt.close();
            // atualiza a porta
            stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, addressPart[0]);
            stmt.setInt(2, DeviceInputCommunicationInterface.SOCKET_INTERFACE_PORT_PARAMETER);
            stmt.setInt(3, applicationId);
            stmt.executeUpdate();
            stmt.close();
            interfaceAddress = new Address(addressPart[1]+":"+addressPart[0]);
            return interfaceAddress;
        } else if (applicationId != 0 && communicationInterface.equals(DeviceInputCommunicationInterface.GOOGLE_CLOUD_MESSAGING)) {
            sql = "UPDATE device_input_communication_parameter_content SET content = '55666' WHERE parameter_id = ? AND application_id = ?;";
            stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, address);
            stmt.setInt(2, DeviceInputCommunicationInterface.GOOGLE_CLOUD_MESSAGING_DEVICE_KEY_PARAMETER);
            stmt.setInt(3, applicationId);
            stmt.executeUpdate();
            stmt.close();
            interfaceAddress = new Address(address);
            return interfaceAddress;
        } else {
            throw new Error("Interface de comunicação não cadastrada");
        }
    }
}
