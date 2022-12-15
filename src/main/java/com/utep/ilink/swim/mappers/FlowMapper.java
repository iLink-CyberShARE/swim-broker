package com.utep.ilink.swim.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.utep.ilink.swim.models.swim.SWIMOutput;
import com.utep.ilink.swim.models.swim.SWIMParameter;
import com.utep.ilink.swim.models.workflow.Flow;
import com.utep.ilink.swim.models.workflow.FlowInput;
import com.utep.ilink.swim.models.workflow.FlowMap;
import com.utep.ilink.swim.models.workflow.InputSpec;

import java.time.LocalDateTime;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
* Creates a list of mappings for the generation of workflow composer inputs.
*/
public class FlowMapper {
    
    private String _flow_id; 
    private FlowInput _flow_input;
    private Datastore _swim_datastore;
    private Datastore _workflow_datastore;

    private List<FlowMap> _mappings;

    private Logger _logger;

    /**
     * Creates mapping dictionaries between SWIM inputs/outputs and workflow composer modules.
     * @param flow_input
     * @param swim_datastore
     * @param workflow_datastore
     */
    public FlowMapper(FlowInput flow_input, Datastore swim_datastore, Datastore workflow_datastore){

        try{

            this._logger = LoggerFactory.getLogger(FlowMapper.class);
            
            // Generate new flow id
            _flow_id = UUID.randomUUID().toString();
            System.out.println("FlowID Created: " + _flow_id);

            _flow_input = flow_input;
            _swim_datastore = swim_datastore;
            _workflow_datastore = workflow_datastore;

            // Initializations
            _mappings = new ArrayList<FlowMap>();
            Flow flow = new Flow();
            flow._id = _flow_id;
            LocalDateTime dateObj = LocalDateTime.now();
            flow.metadata.put("startedAtTime", dateObj.toString());
            flow.metadata.put("status", "initializing");

            // Generate mappings
            this.GenerateInputMappings();
            this.GenerateOutputMappings();

            // save mappings
            this.SaveMappings();

            // Append custom user values
            this.AppendUserValues();

            // verify rules (semantics)

            // add transformations to workflow?

            // Apply explicit rules
            this.ApplyEquivalences();

            flow.metadata.put("status", "processing");
            _workflow_datastore.save(flow);
        }
        catch(Exception e){
            _logger.error(e.toString());
        }

    }

    /**
     * Generates flowmaps from SWIM inputs (i.e. all available model parameters).
     */
    private void GenerateInputMappings() {
        // Get all inputs and add to the mapping list (consider optimizing with hashmaps here)
        Query<SWIMParameter> query = _swim_datastore.find(SWIMParameter.class);
        // Filter by user and scenario
        query.or(
            query.criteria("definitionType").equal("user"),
            query.criteria("definitionType").equal("scenario")
          );
        List<SWIMParameter> params = query.asList();

        for (SWIMParameter parameter : params) {
            FlowMap map = new FlowMap();
            map._id = UUID.randomUUID().toString();
            map.catalogID = UUID.randomUUID().toString();
            map.flowID = _flow_id;
            map.modelID = parameter.modelID;
            map.swimID = parameter.paramName;
            map.type = "input";
            _mappings.add(map);
        }
    }

    /**
     * Generates flowmaps from SWIM outputs (i.e. all possible model outputs).
     */
    private void GenerateOutputMappings() {
        // Get all outputs and add to the mapping list (consider optimizing with hashmaps here)
        Query<SWIMOutput> query = _swim_datastore.find(SWIMOutput.class);
        List<SWIMOutput> outputs = query.asList();

        for (SWIMOutput output : outputs) {
            FlowMap map = new FlowMap();
            map._id = UUID.randomUUID().toString();
            map.catalogID = UUID.randomUUID().toString();
            map.flowID = _flow_id;
            map.modelID = output.modelID;
            map.swimID = output.varName;
            map.type = "output";
            _mappings.add(map);
        }
    }

    /**
     * Saves a list of flow maps to the workflow database.
     */
    private void SaveMappings() {
        _workflow_datastore.save(_mappings);
    }

    /**
     * Adds user defined values to model input mappings. 
     */
    private void AppendUserValues() { 
        for (InputSpec input : _flow_input.inputs) {
            if (input.paramValue != null) {
                // Look for mapping of that input
                Query<FlowMap> query = _workflow_datastore.find(FlowMap.class);
                query.and(
                    query.criteria("flowID").equal(this._flow_id),
                    query.criteria("swimID").equal(input.paramName)
                  );
                FlowMap target = query.filter("swimID", input.paramName).get(); // could be better?
                target.paramValue.put("paramValue", input.paramValue);
                _workflow_datastore.save(target);
            }
        }
    }

    /**
     * Set equivalent elements to have the same catalog id value. 
     * Generates a shared UUID and replaces both catalogIDs.
     */
    private void ApplyEquivalences() {
        // will need a try catch here

        for (Map<String, List<String>> rule: _flow_input.rules) {
            if(rule.containsKey("equivalence")){
                // TODO: validate equivalence here

                // create new composer ID
                String catalogID = UUID.randomUUID().toString();
                for (String swimID : rule.get("equivalence")) {
                    // System.out.println(swimID);
                    // look for swimID and flowID, then replace the value and save.
                    Query<FlowMap> query = _workflow_datastore.find(FlowMap.class);
                    query.and(
                        query.criteria("flowID").equal(this._flow_id),
                        query.criteria("swimID").equal(swimID)
                      );
                    FlowMap target = query.filter("swimID", swimID).get();
                    if (target != null) {
                        target.catalogID = catalogID;
                        _workflow_datastore.save(target);
                    } else {
                        // TODO: handle exception of swim element not found
                    }
                }
            }
        }
    }

    // TODO: Validate Equivalence Rules
    private void ValidateEquivalence() {
        // same type of data structure?

        // same dimensions and size if table?

        // same type of units?

        // semantically fullfills equivalence requirements

    }

    /**** Getters and Setters *****/
    public String GetFlowID() {
        return this._flow_id;
    }
    
}
