/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.communication.output;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import urbosenti.backend.communication.CommunicationInterface;
import urbosenti.backend.communication.MessageWrapper;

/**
 *
 * @author Guilherme
 */
public class SocketCommunicationInterface extends CommunicationInterface {

    public SocketCommunicationInterface() {
        super();
        setId(1);
        setName("Socket Interface");
        setUsesMobileData(false);
        setStatus(CommunicationInterface.STATUS_AVAILABLE);
    }

    @Override
    public boolean testConnection() throws IOException, UnsupportedOperationException {
        try {
            // URL do destino escolhido
            URL url = new URL("http://www.google.com");

            // abre a conexão
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

               // tenta buscar conteúdo da URL
            // se não tiver conexão, essa linha irá falhar
            urlConnect.connect();
            urlConnect.disconnect();
               //Object objData = urlConnect.getContent();

        } catch (UnknownHostException ex) {
            Logger.getLogger(SocketCommunicationInterface.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SocketCommunicationInterface.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean testConnection(URL url) throws IOException, UnsupportedOperationException {
        try {
            // abre a conexão
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
            // tenta buscar conteúdo da URL
            // se não tiver conexão, essa linha irá falhar
            urlConnect.connect();
            urlConnect.disconnect();

        } catch (UnknownHostException ex) {
            Logger.getLogger(SocketCommunicationInterface.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SocketCommunicationInterface.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
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
    public boolean isAvailable() throws IOException, UnsupportedOperationException {
        setStatus(CommunicationInterface.STATUS_AVAILABLE);
        return true;
    }

    @Override
    public Object sendMessageWithResponse(MessageWrapper messageWrapper) throws SocketTimeoutException, IOException {
        Socket socket;
        DataOutputStream outputStream;
        DataInputStream inputStream; 
        String[] address = messageWrapper.getMessage().getTarget().getAddress().split(":");
        // new Socket(host, porta);
        socket = new Socket(address[0], Integer.parseInt(address[1]));
        //cria stream de saída e de entrada
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());
        //envia mensagem
        outputStream.writeUTF(messageWrapper.getEnvelopedMessage());
        outputStream.flush();
        // recebe a mensagem
        String result = inputStream.readUTF();
        // fecha conexão
        outputStream.close();
        inputStream.close();
        socket.close();
        // retorna resultado
        return result;
    }

    @Override
    public boolean sendMessage(MessageWrapper messageWrapper) throws SocketTimeoutException, IOException {
        Socket socket;
        DataOutputStream outputStream;
        System.out.println(messageWrapper.getMessage().getTarget().getAddress());
        String[] address = messageWrapper.getMessage().getTarget().getAddress().split(":");
        // new Socket(host, porta);
        socket = new Socket(address[0], Integer.parseInt(address[1]));
        //cria stream de saída
        outputStream = new DataOutputStream(socket.getOutputStream());
        
        //envia mensagem
        outputStream.writeUTF(messageWrapper.getEnvelopedMessage());
        outputStream.flush();
        outputStream.close();
        socket.close();
        return true;
    }

    @Override
    public Object receiveMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object receiveMessage(int timeout) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
