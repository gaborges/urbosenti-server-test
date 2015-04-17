/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import urbosenti.backend.service.Service;

/**
 *
 * @author Guilherme
 */
public class SensingModule {
    private int id;
    private String uid;
    private String password;
    private Date expirationTime;
    private List<DeviceInputCommunicationInterface> inputCommunicationInterfaces;
    private List<DeviceSetup> deviceSetups;
    
    private Service backendService;

    public SensingModule() {
        inputCommunicationInterfaces = new ArrayList();
        deviceSetups = new ArrayList();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public List<DeviceInputCommunicationInterface> getInputCommunicationInterfaces() {
        return inputCommunicationInterfaces;
    }

    public void setInputCommunicationInterfaces(List<DeviceInputCommunicationInterface> inputCommunicationInterfaces) {
        this.inputCommunicationInterfaces = inputCommunicationInterfaces;
    }

    public List<DeviceSetup> getDeviceSetups() {
        return deviceSetups;
    }

    public void setDeviceSetups(List<DeviceSetup> deviceSetups) {
        this.deviceSetups = deviceSetups;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Service getBackendService() {
        return backendService;
    }

    public void setBackendService(Service backendService) {
        this.backendService = backendService;
    }
    
}
