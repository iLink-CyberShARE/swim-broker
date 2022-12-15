package com.utep.ilink.swim.models.workflow;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InputSpec {

    @NotNull
    @NotEmpty
    public String paramName;

    @NotNull
    @NotEmpty
    public Object paramValue;

    
    // getters and setters

    @JsonProperty("paramName")
    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @JsonProperty("paramValue")
    public Object getParamValue() {
        return paramValue;
    }

    public void setParamValue(Object paramValue) {
        this.paramValue = paramValue;
    }
    
}
