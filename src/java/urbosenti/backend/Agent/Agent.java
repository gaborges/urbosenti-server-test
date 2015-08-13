/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.Agent;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import urbosenti.backend.Storage.ApplicationDAO;
import urbosenti.backend.Storage.InputCommunicationInterfaceDAO;
import urbosenti.backend.Storage.MessagesDAO;
import urbosenti.backend.communication.Address;
import urbosenti.backend.communication.Communication;
import urbosenti.backend.communication.Message;

/**
 *
 * @author Guilherme
 */
public class Agent implements Runnable {

    private final Thread agentThread;
    private final Queue<Message> messages;
    private Communication communication;
    private ApplicationDAO applicationDAO;
    private MessagesDAO messagesDAO;
    private InputCommunicationInterfaceDAO interfaceDAO;

    public Agent() {
        this.messages = new LinkedList();
        this.agentThread = new Thread(this);
    }

    public void setCommunicationManager() {
        communication = Communication.getCommunication();
    }

    public void setMessagesDAO(MessagesDAO messagesDAO) {
        this.messagesDAO = messagesDAO;
    }

    public synchronized void addInteractionMessage(Message message) {
        messages.add(message);
        if (!agentThread.isAlive()) {
            this.agentThread.start();
        }
        notify();
    }

    @Override
    public void run() {
        HashMap<String, Object> values;
        while (true) {
            Message message = null;
            Event event;
            synchronized (this) {
                while (message == null) {

                    message = messages.poll();
                    if (message == null) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            try {
                // extrair mensagem de interação
                event = extractInteractionFromMessageEvent(message);
                System.out.println("Event: "+event);
                // processar o conteúdo das mensagens salvando no banco
                switch (event.getId()) {
                    case 2: // subscribe
                        // verificar se a aplicação existe
                        if (applicationDAO.sensingModuleIsRegistered(event.getParameters().get("uid").toString())) {
                            // salvar no banco a inscrição para receber atualizações
                            applicationDAO.updateSubscription(event.getParameters().get("uid").toString());
                            // atualizar endereço
                            Address address = interfaceDAO.updateAddress(
                                    event.getParameters().get("uid").toString(),
                                    event.getParameters().get("interface").toString(),
                                    event.getParameters().get("address").toString());
                            address.setUid(event.getParameters().get("uid").toString());
                            address.setLayer(Address.LAYER_SYSTEM);
                            // gerar mensagem de aceitação
                            // fazer uma mensagem de aceito caso dê certo. --- ação de interação 6
                            values = new HashMap();
                            values.put("uid", Communication.getBackendService().getUid());
                            values.put("act", "agree'");
                            values.put("type", event.getParameters().get("interface").toString());
                            values.put("target",address);
                            values.put("conversation",1);
                            Action action = new Action();
                            action.setId(6);
                            action.setSynchronous(true);
                            action.setActionType(Event.INTERATION_EVENT);
                            action.setOrigin(Address.LAYER_SYSTEM);
                            action.setParameters(values);
                            // se recebeu atualiza como recebido
                            Communication.getCommunication().applyAction(this.makeInteractionMessage(action));
                        } 
                        

                        break;
                    case 9: // novo endereço
                        // atualiza endereço
                        Address address = interfaceDAO.updateAddress(
                                    event.getParameters().get("uid").toString(),
                                    event.getParameters().get("interface").toString(),
                                    event.getParameters().get("address").toString());
                        // verifica se tem alguma mensagem antiga dele para enviar com upload rate e troca o endereço de destino -- trabalho futuro
                        // messagesDAO.updateTargetAddressMessages(address);
                        // adiciona para envio para o serviço de envio assincrono de mensagens caso exista uma taxa de upload não enviada
                        Double uploadRate = applicationDAO.hasUploadRateToSend(event.getParameters().get("uid").toString());
                        if(uploadRate != null){           
                            // faz uma mensagem para enviar o upload rate
                            values = new HashMap();
                            values.put("uid", Communication.getBackendService().getUid());
                            values.put("act", "agree'");
                            values.put("type", event.getParameters().get("interface").toString());
                            values.put("target",address);
                            values.put("uploadRate",uploadRate);
                            Action action = new Action();
                            action.setId(1);
                            action.setSynchronous(true);
                            action.setActionType(Event.INTERATION_EVENT);
                            action.setOrigin(Address.LAYER_SYSTEM);
                            action.setParameters(values);
                            // se recebeu atualiza como recebido
                            if(Communication.getCommunication().applyAction(this.makeInteractionMessage(action))){                        
                                applicationDAO.updateLastUploadRateAsSent(
                                        applicationDAO.getApplicationIDByUID(
                                                event.getParameters().get("uid").toString()));
                            }
                        }
                        break;
                    case 10: // novo relatório de desempenho
                        // salva no banco
                        // se há algum problema enviar relatório ao ADM do sistema
                        break;
                }

            } catch (NumberFormatException ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("System Message: " + message.getContent() + " " + message.getOrigin().getUid() + " layer: " + message.getOrigin().getLayer());
        }
    }

    // <content><acl>fipa</acl><interactionId>2</interactionId><message><fipa-message act="Subscribe" ><ontology>UrboSenti 1.0</ontology><protocol>UrboSenti-interaction-1.0</protocol><language>xml</language><content><address>{port=55666, ipv4Address=192.168.25.7}</address><interface>Socket</interface><uid>null</uid><layer>System</layer></content></fipa-message></message></content>
    // método para converter e método para ler
    /**
     * Entrai a mensagem do evento. O evento passado por parâmetro representa o
     * evento id=4 do componente de comunicação referente ao recebimento de
     * messagens. Esse evento é um evento de interação e os parâmetros de
     * entrada são:
     * <ul><li>message: urbosenti.core.communication.Message, contém a mensagem,
     * bem como quem enviou a mensagem</li>
     * <li>sender: urbosenti.core.communication.Address, contém o endereço de
     * quem enviou a mensagem</li></ul>
     *
     * @param event
     * @return
     * @throws ClassCastException
     * @throws NumberFormatException
     * @throws SQLException
     * @throws Exception
     */
    public Event extractInteractionFromMessageEvent(Message message) throws ClassCastException, NumberFormatException, SQLException, Exception {
        Event interaction = new InteractionEvent();
        HashMap<String, Object> values = new HashMap();
        /**
         * *** Extrair a mensagem *****
         */
        /* Formato:
         <content>
         <acl>fipa</acl>
         <interactionId>1</interactionId>
         <message>
         <fipa-message act="inform" >
         <ontology>UrboSenti 1.0</ontology>
         <protocol>UrboSenti-interaction-1.0</protocol>
         <language>json</language> 
         <content>{chave:"valor"}</content>
         </fipa-message>
         </message>
         </content> */
        // extrai a acl e verifica qual a linguagem, verificando se o tipo de agente suporta, caso não, exceção
        message.setContent(message.getContent().replace("&gt;", ">"));
        message.setContent(message.getContent().replace("&lt;", "<"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(message.getContent())));
        // <content>
        Element root = doc.getDocumentElement(), messageContent;
        Element content;
        // <acl>fipa</acl>
        // testa se a ACL é conhecida, se não for gera exceção, se sim retorna o id
//        for (AgentCommunicationLanguage acl : acls) {
//            if (dataManager.getAgentCommunicationLanguageDAO()
//                    .isAgentCommunicationLanguageKnown(acl, root.getElementsByTagName("acl").item(0).getTextContent())) {
//                usedAgentCommunicationLanguage = acl;
//                break;
//            } else {
//                throw new Exception("Agent Communication Language '" + root.getElementsByTagName("acl").item(0).getTextContent()
//                        + "' from UID:'" + message.getOrigin().getUid()
//                        + "', addess: '" + message.getOrigin().getAddress() + "' is not supported.");
//            }
//        }
        // <interactionId>1</interactionId>
        int interactionId = Integer.parseInt(root.getElementsByTagName("interactionId").item(0).getTextContent());
        interaction.setId(interactionId);
//        InteractionModel interactionModel = dataManager.getAgentTypeDAO().getInteractionModel(interactionId);
//        if (interactionModel == null) {
//            throw new Exception("Interaction model referred by the value interactionId:'" + interactionId + "' was not found.");
//        }
        /**
         * *** Processar conteúdo segundo Agent Communication Language *****
         */
        // se for FIPA executa esse processo, se houvesse outras a implementação seria dada por esta
        // extrai a mensagem e processa na linguagem conhecida (FIPA)
        //if (usedAgentCommunicationLanguage.getId() == AgentCommunicationLanguage.AGENT_COMMUNICATIVE_LANGUAGE_FIPA_ID) {
        // <fipa-message>
        messageContent = (Element) root.getElementsByTagName("fipa-message").item(0);
        /**
         * A informação de que ato comunicativo está sendo passado é despresível
         * por causa do interactionId
         * <fipa-message act="inform" >
         * String communicativeAct = messageContent.getAttribute("act");
         */
        /**
         * Se for 2 então é inscrição
         */
        //<content><chave>valor</chave></content>
        content = (Element) messageContent.getElementsByTagName("content").item(0);
        if (interactionId == 2) {
            /*
             Endereço do Assinante String String                 address
             Interface de entrada  String {GCM, Socket}          interface
             UID do assinante      String String                 uid
             Camada                String {System, Application}	layer
             */
            String parameters[] = {"address", "interface", "uid", "layer"};
            for (String p : parameters) {
                values.put(p, content.getElementsByTagName(p).item(0).getTextContent());
            }
        } else if (interactionId == 9) { //Se for 9 então atualizar endereço de entrada
            /*
             UID do assinante            String	String          uid
             Interface de entrada	String	{GCM, Socket}	interface
             Endereço do Assinante	String	String          address
             */
            String parameters[] = {"address", "interface", "uid"};
            for (String p : parameters) {
                values.put(p, content.getElementsByTagName(p).item(0).getTextContent());
            }
        }
        values.put("origin", message.getOrigin());
        values.put("message", message);
            // Processar conteúdo da mensagem adicionando os elementos como parâmetros no HashMap do objetivo event

        // }
        // adiciona o interaction ID no evento
        interaction.setId(interactionId);
        interaction.setParameters(values);
        // retorna o evento de interação com os parâmetros e o interaction id referentes do Interaction Model
        return interaction;
    }

//    private Event proccessFIPAInteractionMessage (Element root){
//        
//    }
    public Action makeInteractionMessage(Action action) throws ClassCastException, NumberFormatException, SQLException, Exception {
        String finalString;
        /**
         * *** Extrair a mensagem *****
         */
        //<content> -- será adicionado na mensagem
        // <acl>fipa</acl>
        finalString = "<acl>fipa</acl>";
        // <interactionId>1</interactionId>
        finalString += "<interactionId>" + action.getId() + "</interactionId>";
        // <message>
        finalString += "<message>";
        //<fipa-message act="inform" >
        finalString += "<fipa-message act=\"" + action.getParameters().get("act") + "\" >";
        // <ontology>UrboSenti 1.0</ontology>
        finalString += "<ontology>UrboSenti 1.0</ontology>";
        // <protocol>UrboSenti-interaction-1.0</protocol>
        finalString += "<protocol>UrboSenti-interaction-1.0</protocol>";
        // <language>xml</language> 
        finalString += "<language>xml</language>";
        // <content>{chave:"valor"}</content>
        finalString += "<content>";

        //for (Parameter p : interactionModel.getParameters()) {
        //    finalString += "<" + p.getLabel() + ">";
        //    finalString += action.getParameters().get(p.getLabel();
        //     finalString += "</" + p.getLabel() + ">";
        //}
        if (action.getId() == 1) {
            /* Atualizar taxa de upload
             Taxa de upload	double	0.0 – 1.0	uploadRate
             UID do servidor	String	String          uid
             */
            finalString += "<uploadRate>" + action.getParameters().get("uploadRate") + "</uploadRate>";
            finalString += "<uid>" + action.getParameters().get("uid") + "</uid>";
        } else if (action.getId() == 6) {
            /* Assinatura aceita
             Id da conversa primária	int	unsigned int	conversation
             UID do servidor             String	String          uid
             */
            finalString += "<conversation>" + action.getParameters().get("conversation") + "</conversation>";
            finalString += "<uid>" + action.getParameters().get("uid") + "</uid>";
        }
        finalString += "</content>";
        // </fipa-message>
        finalString += "</fipa-message>";
        // </message>
        finalString += "</message>";
        /**
         * * alterar o action **
         */
        Message message = new Message();
        message.setContent(finalString);
        message.setTarget((Address) action.getParameters().get("target"));
        message.setOrigin(new Address());
        message.getOrigin().setUid(Communication.getBackendService().getUid());
        message.getOrigin().setLayer(Address.LAYER_SYSTEM);
        message.setSubject(Message.SUBJECT_SYSTEM_INTERACTION);
        message.setContentType("text/xml");
        message.setUsesUrboSentiXMLEnvelope(true);
        // Preparar ação para envio: envio de mensagens assíncrona
        action.getParameters().put("message", message);
        if (action.isSynchronous()) {
            action.setId(1);
        } else {
            action.setId(2);
        }
        action.setOrigin(Address.LAYER_SYSTEM);
//        action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
//        action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);
        action.setTargetComponentId(1);
        action.setTargetEntityId(1);
        action.setActionType(Event.INTERATION_EVENT);
        return action;
    }

    public void setApplicationDAO(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    public void setInterfaceDAO(InputCommunicationInterfaceDAO interfaceDAO) {
        this.interfaceDAO = interfaceDAO;
    }
    
}
