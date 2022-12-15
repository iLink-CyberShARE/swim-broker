package com.utep.ilink.swim.models.workflow;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Id;

public class Provenance {
    @Id public String _id;
    public String entity; 
    public String wasGeneratedBy;
    public String generatedAtTime;
    public List<Object> wasDerivedFrom;

    public Provenance() {
        wasDerivedFrom = new ArrayList<Object>();
    }

}
