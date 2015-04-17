/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.communication;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;

/**
 * REST Web Service
 *
 * @author Guilherme
 */
@Path("backend")
public class BackendWebService {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public BackendWebService() {
    }

    /**
     * Retrieves representation of an instance of br.com.ufrgs.BackendWebService
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml() {
        //TODO return proper representation object
        return "<lol><eu>guilherme</eu></lol>";
    }

    /**
     * PUT method for updating or creating an instance of BackendWebService
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes({"application/xml", "text/plain"})
    public void putXml(String content) {
        System.out.println(content);
        // gerar mensagem em XML
        Message message = Communication.removingEnvelope(content);

    }

    /**
     * PUT method for updating or creating an instance of BackendWebService
     *
     * @param content representation for the resource
     * @param headers
     * @return
     */
    @POST
    @Consumes({"text/xml", "text/plain", "application/xml"})
    @Produces({"text/xml", "application/xml"})
    public String postXml(String content) {
        Long initialTime = System.currentTimeMillis();
//        Date ini = new Date();
//        long iniNanoTime = System.nanoTime();
        //System.out.println(content);
        //System.out.println(headers.getRequestHeader("Date").get(0));
        // gerar mensagem em XML
        Message message = Communication.removingEnvelope(content);
        String returnedContent = "<error>sim</error>";
        Communication communication = Communication.getCommunication();
        switch (message.getSubject()) {
            case Message.SUBJECT_REGISTRATION:
                message = communication.registerDevice(message);
                if (message != null) {
                    returnedContent = Communication.addResponseEnvelope(System.currentTimeMillis() - initialTime, message);
                }
                break;
            case Message.SUBJECT_CANCEL_REGISTRATION:
                message = communication.cancelDeviceRegister(message);
                if (message != null) {
                    returnedContent = Communication.addResponseEnvelope(System.currentTimeMillis() - initialTime, message);
                }
                break;
            case Message.SUBJECT_UPLOAD_REPORT:
                message = communication.newDeviceReport(message);
                if (message != null) {
                    returnedContent = communication.addResponseEnvelopeForReports(System.currentTimeMillis() - initialTime, message);
                    //returnedContent = communication.addResponseEnvelopeForReports((System.nanoTime()-iniNanoTime), message);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(BackendWebService.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    System.out.println("Current-"+(System.currentTimeMillis() - initialTime));
//                    System.out.println("Nano-"+(System.nanoTime()-iniNanoTime));
//                    System.out.println("Date-"+((new Date()).getTime()-ini.getTime()));
                }
                break;
            case Message.SUBJECT_SYSTEM_INTERACTION:
                message = communication.cancelDeviceRegister(message);
                if (message != null) {
                    returnedContent = Communication.systemInteractionMessage(System.currentTimeMillis() - initialTime, message);
                }
                break;
            case Message.SUBJECT_APPLICATION_DEFINED:
                message = communication.cancelDeviceRegister(message);
                if (message != null) {
                    returnedContent = Communication.applicationDefinedMessage(System.currentTimeMillis() - initialTime, message);
                }
                break;
        }
        if (returnedContent == null) {
            return "<error>sim</error>";
        } else {
            return returnedContent;
        }
    }

    @PUT
    @Path("/return")
    @Consumes("application/xml")
    @Produces("application/xml")
    public String putXmlWithReturn(String content) {
        System.out.println("Content: " + content);

        String response = "<message>\n"
                + "	<header>\n"
                + "		<origin>\n"
                + "			<uid>11XYZ</uid>\n"
                + "			<name>Backend Module</name>\n"
                + "			<address>192.168.0.1</address>\n"
                + "			<layer>system</layer>\n"
                + "               </origin>\n"
                + "               <target>\n"
                + "			<uid>22XYZ</uid>\n"
                + "                       <address>192.168.0.2</address>\n"
                + " 			<layer>system</layer>\n"
                + "               </target>\n"
                + "               <subject>social interaction</subject>\n"
                + "               <contentType>text/xml</contentType>\n"
                + "	</header>\n"
                + "	<content>"
                + "		… message according the subject"
                + "       </content>"
                + "     </message>";
        return response;
    }
}
