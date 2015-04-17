/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.communication;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import urbosenti.backend.Storage.ApplicationDAO;
import urbosenti.backend.Storage.InputCommunicationInterfaceDAO;
import urbosenti.backend.Storage.ReportDAO;
import urbosenti.backend.service.Service;
import urbosenti.backend.service.model.DeviceInputCommunicationInterface;
import urbosenti.backend.service.model.DeviceSetup;
import urbosenti.backend.service.model.ExtraParameter;
import urbosenti.backend.service.model.SensingModule;
import urbosenti.backend.util.DBConnection;

/**
 *
 * @author Guilherme
 */
public class Communication {

    private static Communication communication = null;
    private static final Service backendService = new Service(1,"d428f2f7-ae09-4ecf-b57a-b3eaf2362d38");
    private ApplicationDAO applicationDAO;
    private InputCommunicationInterfaceDAO interfaceDAO;
    private ReportDAO reportDAO;

    static String systemInteractionMessage(long l, Message message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    static String applicationDefinedMessage(long l, Message message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Communication() {
        communication = null;
        DBConnection dbc = new DBConnection();
        this.applicationDAO = new ApplicationDAO(dbc);
        this.interfaceDAO = new InputCommunicationInterfaceDAO(dbc);
        this.reportDAO = new ReportDAO(dbc);
    }

    public synchronized static Communication getCommunication() {
        if (communication == null) {
            communication = new Communication();
        }
        return communication;
    }

    public static Message removingEnvelope(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            // <message requireResponse="false">
            Element response = doc.getDocumentElement();
            Message msg = new Message();
            msg.setOrigin(new Address());
            msg.setTarget(new Address());
            // requireResponse
            if (response.hasAttribute("requireResponse")) {
                msg.setRequireResponse(response.getAttribute("requireResponse").equals("true"));
            }
            //<header>
            Element header = (Element) response.getElementsByTagName("header").item(0);
            // <origin> -> <uid>
            msg.getOrigin().setUid(((Element) header.getElementsByTagName("origin").item(0)).getElementsByTagName("uid").item(0).getTextContent());
            // <origin> -> <layer>
            msg.getOrigin().setLayer(Integer.parseInt(((Element) header.getElementsByTagName("origin").item(0)).getElementsByTagName("layer").item(0).getTextContent()));
            // <target> -> <uid>
            msg.getTarget().setUid(((Element) header.getElementsByTagName("target").item(0)).getElementsByTagName("uid").item(0).getTextContent());
            // <target> -> <layer>
            msg.getTarget().setLayer(Integer.parseInt(((Element) header.getElementsByTagName("target").item(0)).getElementsByTagName("layer").item(0).getTextContent()));
            // <priority>
            if (header.getElementsByTagName("priority").getLength() > 0) {
                if (header.getElementsByTagName("priority").item(0).getTextContent().equals(String.valueOf(Message.PREFERENTIAL_PRIORITY))) {
                    msg.setPreferentialPriority();
                } else {
                    msg.setNormalPriority();
                }
            }
            //<subject>
            msg.setSubject(Integer.parseInt(header.getElementsByTagName("subject").item(0).getTextContent()));
            //<contentType>
            msg.setContentType(header.getElementsByTagName("contentType").item(0).getTextContent());
            //<contentSize> -- utilizado somente para comparar;

            //<anonymousUpload>
            if (header.getElementsByTagName("anonymousUpload").getLength() > 0) {
                msg.setAnonymousUpload(Boolean.parseBoolean(header.getElementsByTagName("anonymousUpload").item(0).getTextContent()));
            } else {
                msg.setAnonymousUpload(false);
            }
            StringWriter writer = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(response.getElementsByTagName("content").item(0)), new StreamResult(writer));
            // <content> - conteúdo da mensagem
            msg.setContent( writer.toString());
            return msg;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String addResponseEnvelope(Long processingTime, Message message) {
        try {
            // gerar mensagem em XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            // <message>
            Element root = doc.createElement("message"),
                    header = doc.createElement("header"),
                    contentElement = doc.createElement("content"),
                    contentType = doc.createElement("contentType"),
                    contentSize = doc.createElement("contentSize"),
                    processingTimeElement; // Converter Documento para STRING
            // <contentType>
            contentType.setTextContent(message.getContentType());
            // <header> -> <contentType>
            header.appendChild(contentType);
            // <contentSize>
            contentSize.setTextContent(String.valueOf(message.getContentSize()));
            // <header> -> <contentSize>
            header.appendChild(contentSize);
            // <performanceMeasure>
            processingTimeElement = doc.createElement("performanceMeasure");
            // <performanceMeasure metric="processingTime">
            processingTimeElement.setAttribute("name", "processingTime");
            processingTimeElement.setTextContent(processingTime.toString());
            // <header> -> <performanceMeasure>
            header.appendChild(processingTimeElement);
            // <message> -> <header>
            root.appendChild(header);
            // <content>
            contentElement.setTextContent(message.getContent());
            // <message> -> <header>
            root.appendChild(contentElement);
            // adiciona no documento
            doc.appendChild(root);
            // transforma o documento em string
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(doc), new StreamResult(stw));
            // retorna a mensagem pronta para envio
            String res = stw.getBuffer().toString().replace("&gt;", ">");
            res = res.replace("&lt;", "<");
            return res;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String addResponseEnvelopeForReports(Long processingTime, Message message) {
        Long i = System.currentTimeMillis();
        String r = addResponseEnvelope(processingTime, message);
        try {
            this.reportDAO.updateReportResponseTime(processingTime + (i-System.currentTimeMillis()), message.getTarget().getUid());
        } catch (SQLException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }

    public Message registerDevice(Message message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message.getContent())));
                  //  responseDoc = builder.newDocument();
            Element device;
            device = doc.getDocumentElement();
            SensingModule sensingModule = new SensingModule();
            sensingModule.setBackendService(backendService);
            // processar os dados.
            // <device>
            // <deviceSetup name="cpuModel">arm64bits</deviceSetup>
            NodeList nodes = device.getElementsByTagName("deviceSetup"), subNodes;
            for (int i = 0; i < nodes.getLength(); i++) {
                DeviceSetup deviceSetup = new DeviceSetup();
                // cpuModel
                if (((Element) nodes.item(i)).getAttribute("name").equals("cpuModel")) {
                    deviceSetup.setSetupId(1);
                    deviceSetup.setLabel("cpuModel");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // cpuCore
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("cpuCore")) {
                    deviceSetup.setSetupId(2);
                    deviceSetup.setLabel("cpuCore");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // cpuClock
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("cpuClock")) {
                    deviceSetup.setSetupId(3);
                    deviceSetup.setLabel("cpuClock");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // deviceModel
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("deviceModel")) {
                    deviceSetup.setSetupId(4);
                    deviceSetup.setLabel("deviceModel");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // nativeOS
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("nativeOS")) {
                    deviceSetup.setSetupId(5);
                    deviceSetup.setLabel("nativeOS");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // storage
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("storage")) {
                    deviceSetup.setSetupId(6);
                    deviceSetup.setLabel("storage");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // battery
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("battery")) {
                    deviceSetup.setSetupId(7);
                    deviceSetup.setLabel("battery");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                    // memory
                } else if (((Element) nodes.item(i)).getAttribute("name").equals("memory")) {
                    deviceSetup.setSetupId(8);
                    deviceSetup.setLabel("memory");
                    deviceSetup.setContent(nodes.item(i).getTextContent());
                    sensingModule.getDeviceSetups().add(deviceSetup);
                }
            }
            // <inputCommunicationInterface type="Socket">
            nodes = device.getElementsByTagName("inputCommunicationInterface");
            for (int i = 0; i < nodes.getLength(); i++) {
                DeviceInputCommunicationInterface communicationInterface = new DeviceInputCommunicationInterface();
                if (nodes.item(i).getAttributes().getNamedItem("type").getTextContent().equals("Socket")) {
                    communicationInterface.setId(1);
                    communicationInterface.setType("Socket");
                    // <extra name="ipv4Address">192.168.1.1</extra>
                    subNodes = ((Element)nodes.item(i)).getElementsByTagName("extra");
                    for(int j = 0; j < subNodes.getLength();j++){
                        if(subNodes.item(j).getAttributes().getNamedItem("name").getTextContent().equals("ipv4Address")){
                            ExtraParameter ep = new ExtraParameter();
                            ep.setLabel("ipv4Address");
                            ep.setParameterId(1);
                            ep.setContent(subNodes.item(0).getTextContent());
                            communicationInterface.getExtraParameters().add(ep);
                        } else if(subNodes.item(j).getAttributes().getNamedItem("name").getTextContent().equals("port")){
                            ExtraParameter ep = new ExtraParameter();
                            ep.setLabel("port");
                            ep.setParameterId(2);
                            ep.setContent(subNodes.item(0).getTextContent());
                            communicationInterface.getExtraParameters().add(ep);
                        }
                    }
                    sensingModule.getInputCommunicationInterfaces().add(communicationInterface);
                } else if (nodes.item(i).getAttributes().getNamedItem("type").getTextContent().equals("GCM")) {
                    communicationInterface.setId(2);
                    communicationInterface.setType("GCM");
                    // <extra name="ipv4Address">192.168.1.1</extra>
                    subNodes = ((Element)nodes.item(i)).getElementsByTagName("extra");
                    for(int j = 0; j < subNodes.getLength();j++){
                        if(subNodes.item(j).getAttributes().getNamedItem("name").getTextContent().equals("deviceKey")){
                            ExtraParameter ep = new ExtraParameter();
                            ep.setLabel("deviceKey");
                            ep.setParameterId(3);
                            ep.setContent(subNodes.item(0).getTextContent());
                            communicationInterface.getExtraParameters().add(ep);
                        }
                    }
                    sensingModule.getInputCommunicationInterfaces().add(communicationInterface);
                }
            }            
            // Criar a senha e o UUID
            sensingModule.setUid(UUID.randomUUID().toString());
            sensingModule.setPassword(String.valueOf(System.currentTimeMillis())+sensingModule.getUid().subSequence(5, 10));
            // Salvar os dados
            this.applicationDAO.insertSensingModule(sensingModule);
            this.applicationDAO.insertSensingModuleSetups(sensingModule);
            this.interfaceDAO.insertInputCommunicationExtraParameters(sensingModule);
            // inicia mensagem de resposta
//            Element response = doc.createElement("registry"),
//                    applicationUid = doc.createElement("applicationUid"),
//                    serviceUid = doc.createElement("serviceUid"),
//                    password = doc.createElement("password"),
//                    expirationTime = doc.createElement("expirationTime");
            /************** Conteúdo de retorno ***************/
            //<registry>
            String registry = "<registry>";
            //<applicationUid>uid da aplicação</applicationUid>
//            applicationUid.setTextContent(sensingModule.getUid());
            registry += "<applicationUid>"+sensingModule.getUid()+"</applicationUid>";
            //<serviceUid>uid do backend</serviceUid>
//            serviceUid.setTextContent(sensingModule.getBackendService().getUid());
            registry +="<serviceUid>"+sensingModule.getBackendService().getUid()+"</serviceUid>";
            //<password>123456</password>
//            password.setTextContent(sensingModule.getPassword());
            registry += "<password>"+sensingModule.getPassword()+"</password>";
            //<expirationTime>0</expirationTime>
//            expirationTime.setTextContent("0");
            registry += "<expirationTime>0</expirationTime>";
            registry += "</registry>";
            // Aticional todos no <registry>
//            response.appendChild(applicationUid);
//            response.appendChild(serviceUid);
//            response.appendChild(password);
//            response.appendChild(expirationTime);
//            responseDoc.appendChild(response);
            
//            StringWriter stw = new StringWriter();
//            Transformer serializer = TransformerFactory.newInstance().newTransformer();
//            serializer.transform(new DOMSource(response), new StreamResult(stw));
            
            Message responseMessage = new Message();
            //responseMessage.setContent(stw.getBuffer().toString());
            responseMessage.setContent(registry);
            responseMessage.setContentType("text/xml");
            return responseMessage;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Message cancelDeviceRegister(Message message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Message newDeviceReport(Message message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message.getContent())));
            Element report = doc.getDocumentElement();
            int userId = 0, reportId;
            if(report.hasAttribute("userId")){
                if(report.getAttribute("userId").length() > 0){
                    userId = Integer.parseInt(report.getAttribute("userId"));
                }
            }
            if(userId > 0){
                reportId = this.reportDAO.insertGenericReport(message.getOrigin().getUid(), report.getTextContent(), userId);
            } else {
                reportId = this.reportDAO.insertGenericReport(message.getOrigin().getUid(), report.getTextContent());
            }
            Message responseMessage = new Message();
            responseMessage.setContent("");
            responseMessage.setContentType("text/xml");
            responseMessage.setTarget(message.getOrigin());
            responseMessage.setOrigin(message.getTarget());
            return responseMessage;
        } catch (SAXException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
