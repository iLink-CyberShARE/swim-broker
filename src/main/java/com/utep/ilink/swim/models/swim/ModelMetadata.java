package com.utep.ilink.swim.models.swim;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("model-catalog")
public class ModelMetadata {
    @Id String _id;
    public List<Object> info;
    public String dateCreated;
    public String dateModified;
    public String softwareAgent;
    public String license;
    public String version;
    public String sponsor;
    public List<Object> creators;
    public HostServer hostServer;
    public ServiceInfo serviceInfo;

    public ModelMetadata(){
        creators = new ArrayList<Object>();
        info = new ArrayList<Object>();
        hostServer = new HostServer();
        serviceInfo = new ServiceInfo();
    }

}





