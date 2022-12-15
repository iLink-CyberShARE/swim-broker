package com.utep.ilink.swim.models.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
    public String id;
    public List<Map<String, Object>> inputs;
    public List<Map<String, Object>> outputs;
    public Map<String, Object> computationInfo;

    public Model(){
        inputs = new ArrayList<Map<String, Object>>();
        outputs = new ArrayList<Map<String, Object>>();
        computationInfo = new HashMap<String, Object>();
    } 

}
