package com.utep.ilink.swim.models.swim;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
    public String serviceURL;
    public String serviceMethod;
    public String status;
    public String consumes;
    public String produces;
    public Boolean isPublic;
    public List<String> externalDocs;

    public ServiceInfo(){
        externalDocs = new ArrayList<String>();
    }
    
}
