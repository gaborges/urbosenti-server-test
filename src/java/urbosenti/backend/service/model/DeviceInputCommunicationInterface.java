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
