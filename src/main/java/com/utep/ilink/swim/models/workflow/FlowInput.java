package com.utep.ilink.swim.models.workflow;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlowInput {

    @NotNull
    public List<InputSpec> inputs;

    @NotNull
    @Size(min=1, max=200)
    public List<@Valid OutputSpec> outputs;

    @NotNull
    public List<Map<String, List<String>>> rules;

    // Constructors
    /*
    public FlowInput(){
        // need to instantiate hashmap here?
    }
    */

    // Getters and Setters

    @JsonProperty("inputs")
    public List<InputSpec> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputSpec> inputs) {
        this.inputs = inputs;
    }

    @JsonProperty("outputs")
    public List<OutputSpec> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<OutputSpec> outputs) {
        this.outputs = outputs;
    }

    @JsonProperty("rules")
    public List<Map<String, List<String>>> getRules() {
        return rules;
    }

    public void setRules(List<Map<String, List<String>>> rules) {
        this.rules = rules;
    }


}
