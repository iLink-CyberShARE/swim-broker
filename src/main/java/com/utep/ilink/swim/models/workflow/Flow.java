package com.utep.ilink.swim.models.workflow;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("flows")
public class Flow {
    @Id public String _id;   
    public Map<String,Object> metadata; 
    public List<Object> provenance;

    public Flow(){
        metadata = new HashMap<String, Object>();
        provenance = new ArrayList<Object>();
    }

}
