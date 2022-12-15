package com.utep.ilink.swim.models.workflow;

import java.util.HashMap;
import java.util.Map;

public class Transformation {
    public String id;
    public Map<String, Object> computationInfo;

    public Transformation() {
        computationInfo = new HashMap<String, Object>();
    }

}
