package com.utep.ilink.swim.models.workflow;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.HashMap;
import java.util.Map;

@Entity("flowmappings")
public class FlowMap {
    public @Id String _id;

    public String flowID;
    public String modelID;
    public String catalogID;
    public String swimID;
    public String type;
    public Map<String,Object> paramValue;

    public FlowMap(){
        paramValue = new HashMap<String, Object>();
    }

} 
