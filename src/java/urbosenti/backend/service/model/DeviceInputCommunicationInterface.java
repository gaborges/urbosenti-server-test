/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.service.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilherme
 */
public class DeviceInputCommunicationInterface {
    public static final String SOCKET_INTERFACE = "Socket";
    public static final int SOCKET_INTERFACE_PORT_PARAMETER = 2;
    public static final int SOCKET_INTERFACE_IPV4_ADDRESS_PARAMETER = 1;
    public static final String GOOGLE_CLOUD_MESSAGING = "GCM";
    public static final int GOOGLE_CLOUD_MESSAGING_DEVICE_KEY_PARAMETER = 3;
    private int id;
    private String type;
    private List<ExtraParameter> extraParameters;

    public DeviceInputCommunicationInterface() {
        this.extraParameters = new ArrayList();
    }
   
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ExtraParameter> getExtraParameters() {
        return extraParameters;
    }

    public void setExtraParameters(List<ExtraParameter> extraParameters) {
        this.extraParameters = extraParameters;
    }
    
}
