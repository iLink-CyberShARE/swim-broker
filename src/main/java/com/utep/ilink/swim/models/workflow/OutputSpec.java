package com.utep.ilink.swim.models.workflow;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutputSpec{

    @NotNull
    @NotEmpty
    public String varName;

    @JsonProperty("varName")
    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    
}
