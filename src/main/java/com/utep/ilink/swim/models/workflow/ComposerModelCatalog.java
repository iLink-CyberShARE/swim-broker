package com.utep.ilink.swim.models.workflow;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity("composercatalogs")
public class ComposerModelCatalog {
    @Id String _id;
    
    public String context;
    public String flowid;
    public List<Transformation> transformations;
    public List<Model> models;

    public ComposerModelCatalog(){
        models = new ArrayList<Model>();
    }

    /**
     * Returns object serialized as JSON string
     * @throws JsonProcessingException
     */
    public String toJSONString() throws JsonProcessingException{
        return new ObjectMapper().writeValueAsString(this);
    }

}


