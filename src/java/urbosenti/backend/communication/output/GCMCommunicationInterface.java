/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.communication.output;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import urbosenti.backend.communication.CommunicationInterface;
import urbosenti.backend.communication.MessageWrapper;

/**
 *
 * @author Guilherme
 */
public class GCMCommunicationInterface extends CommunicationInterface{

    public GCMCommunicationInterface() {
        super();
        setId(2);
        setName("Google Cloud Messaging Interface");
        setUsesMobileData(false);
        setStatus(CommunicationInterface.STATUS_AVAILABLE);
    }   
    
    @Override
    public boolean isAvailable() throws UnsupportedOperationException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean testConnection() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean testConnection(URL url) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean connect(String address) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean disconnect() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sendMessage(MessageWrapper messageWrapper) throws SocketTimeoutException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object sendMessageWithResponse(MessageWrapper messageWrapper) throws SocketTimeoutException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object receiveMessage() throws SocketTimeoutException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object receiveMessage(int timeout) throws SocketTimeoutException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
