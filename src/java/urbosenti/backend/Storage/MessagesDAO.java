/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.Storage;

import java.sql.Connection;
import java.util.ArrayList;
import urbosenti.backend.communication.Address;
import urbosenti.backend.communication.CommunicationInterface;
import urbosenti.backend.communication.Message;
import urbosenti.backend.communication.MessageWrapper;
import urbosenti.backend.util.DBConnection;

/**
 *
 * @author Guilherme
 */
public class MessagesDAO {
    
    private final DBConnection dbc;
    private final Connection connection;
    
    public MessagesDAO(DBConnection dbc) {
        this.dbc = dbc;
        connection = dbc.getConnection();
    }

    public ArrayList<MessageWrapper> getMessagesNotSent(ArrayList<CommunicationInterface> communicationInterfaces) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void insert(MessageWrapper messageWrapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int countMessagesToSend() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void updateMessageAsSent(MessageWrapper mw) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void updateTargetAddressMessages(Address address) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
