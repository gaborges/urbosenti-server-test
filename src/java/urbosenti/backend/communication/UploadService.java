/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import urbosenti.backend.Storage.MessagesDAO;

/**
 *
 * @author Guilherme
 */
class UploadService implements Runnable {

    private final Queue<MessageWrapper> queueOfMessages;
    private ArrayList<MessageWrapper> storedMessagesToSend;
    private Thread thread = null;
    private MessagesDAO messagesDAO;
    private ArrayList<CommunicationInterface> communicationInterfaces;

    public UploadService() {
        this.messagesDAO = null;
        queueOfMessages = new LinkedList();
    }

    public void setMessagesDAO(MessagesDAO messagesDAO) {
        this.messagesDAO = messagesDAO;
    }

    public void setCommunicationInterfaces(ArrayList<CommunicationInterface> communicationInterfaces) {
        this.communicationInterfaces = communicationInterfaces;
    }

    @Override
    public void run() {
        Iterator<MessageWrapper> iterator = null;
        boolean messageQueue = true;
        while (true) {
            MessageWrapper mw;
            if (storedMessagesToSend != null) {
//                if(iterator == null){
//                    iterator = storedMessagesToSend.iterator();
//                }
//                if(iterator.hasNext()){
//                    
//                }
            }
            // verifica a cada ciclo se há alguma mensagem para enviar na queueOfMessages e na storedMessagesToSend,
            while (true) {
                if (messagesDAO != null) {
                    this.storedMessagesToSend = this.messagesDAO.getMessagesNotSent(this.communicationInterfaces);
                    iterator = storedMessagesToSend.iterator();
                }
                if (messageQueue) {
                    mw = queueOfMessages.poll();
                    if (mw == null) {
                        if (this.messagesDAO == null) {
                            synchronized (this) {
                                try {
                                    wait();
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(UploadService.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        } else {
                            messageQueue = false;
                        }
                    } else {
                        messageQueue = false;
                        break;
                    }
                } else {
                    if (this.messagesDAO != null) {
                        if (iterator.hasNext()) {
                            mw = iterator.next();
                            messageQueue = true;
                            break;
                        } else {
                            if (this.messagesDAO.countMessagesToSend() > 0) {
                                this.storedMessagesToSend = this.messagesDAO.getMessagesNotSent(this.communicationInterfaces);
                                iterator = storedMessagesToSend.iterator();
                                messageQueue = true;
                            } else {
                                synchronized (this) {
                                    if (this.queueOfMessages.size() == 0) {
                                        try {
                                            wait();
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(UploadService.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        messageQueue = true;
                    }
                }
            }
            try {
                // se não há busca se há alguma que não foi enviada no banco. Se não retornar nada dorme. Acorda se alguma for colocada na fila
                // quando enviar atualiza que enviou na tabela de mensagens
                mw.getUsedCommunicationInterface().sendMessage(mw);
                // armazena como entregue
                this.messagesDAO.updateMessageAsSent(mw);
            } catch (IOException ex) {
                Logger.getLogger(UploadService.class.getName()).log(Level.SEVERE, null, ex);
                // se não conseguir entregar uma da fila ela não retorna para ela.
            }
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public synchronized void sendAssynchronousMessage(Message message, CommunicationInterface communicationInterface) {
        MessageWrapper messageWrapper = new MessageWrapper(message);
        messageWrapper.setUsedCommunicationInterface(communicationInterface);
        try {
            messageWrapper.build();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(UploadService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(UploadService.class.getName()).log(Level.SEVERE, null, ex);
        }
        // armazena
        if (this.messagesDAO != null) {
            this.messagesDAO.insert(messageWrapper);
        }
        // coloca na fila
        this.queueOfMessages.add(messageWrapper);
        // acorda o serviço
        notifyAll();
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(thread);
            thread.setDaemon(true);
            thread.start();
        }
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    public void stop() {
        thread.interrupt();
    }
}
