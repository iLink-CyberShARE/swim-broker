package com.utep.ilink.swim.models.swim;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("parameter-catalog")
public class SWIMParameter {
    @Id String _id;
    public String modelID;
    public String dataType;
    public String paramName;
    public String definitionType;
    public int maxValue;
    public int minValue;
    public String structType;
    public List<ParamInfo> paraminfo;
}


