package com.utep.ilink.swim.models.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mongodb.morphia.annotations.Entity;

@Entity("composerinputs")
public class ComposerInput {
    public String flowid;
    public List<Map<String, Object>> inputs;
    public List<Map<String, Object>> outputs;

    public ComposerInput(){
        inputs = new ArrayList<Map<String, Object>>();
        outputs = new ArrayList<Map<String, Object>>();
    }

    /**
     * Returns object serialized as JSON string
     * @throws JsonProcessingException
     */
    public String toJSONString() throws JsonProcessingException{
        return new ObjectMapper().writeValueAsString(this);
    }

}
