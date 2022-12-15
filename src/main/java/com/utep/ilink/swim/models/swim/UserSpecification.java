package com.utep.ilink.swim.models.swim;

import java.util.ArrayList;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSpecification {

    ArrayList<HashMap<String, String>> modelSettings;

    public UserSpecification() { }

    public UserSpecification(ArrayList<HashMap<String, String>> modelSettings) {
        this.modelSettings = modelSettings;
    }

    public ArrayList<HashMap<String, String>> getModelSettings() {
        return modelSettings;
    }

    public String getModelID() {
        return modelSettings.get(0).get("modelID");
    }
}