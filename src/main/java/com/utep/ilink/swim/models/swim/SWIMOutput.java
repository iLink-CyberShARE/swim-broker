package com.utep.ilink.swim.models.swim;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("output-catalog")
public class SWIMOutput {
    @Id String _id;
    public String modelID;
    public String varName;
    public List<VarInfo> varinfo;
}
