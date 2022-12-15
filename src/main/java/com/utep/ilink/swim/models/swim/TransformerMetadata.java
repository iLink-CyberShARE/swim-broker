package com.utep.ilink.swim.models.swim;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("transformation-catalog")
public class TransformerMetadata {
    @Id public String _id;
    public List<Object> info;
    public String dateCreated;
    public String dateModified;
    public String language;
    public String license;
    public String version;
    public List<Object> creators;
    public ServiceInfo serviceInfo;

    public TransformerMetadata(){
        creators = new ArrayList<Object>();
        info = new ArrayList<Object>();
        serviceInfo = new ServiceInfo();
    }

}





