/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.backend.service;

/**
 *
 * @author Guilherme
 */
public final class Service {
   private int id;
   private final String uid;

    public Service(int id, String uid) {
        this.id = id;
        this.uid = uid;
    }
    
    public Service(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
   
}
