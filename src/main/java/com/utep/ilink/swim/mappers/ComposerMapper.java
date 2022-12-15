package com.utep.ilink.swim.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utep.ilink.swim.models.swim.ModelMetadata;
import com.utep.ilink.swim.models.swim.TransformerMetadata;
import com.utep.ilink.swim.models.workflow.ComposerInput;
import com.utep.ilink.swim.models.workflow.ComposerModelCatalog;
import com.utep.ilink.swim.models.workflow.FlowInput;
import com.utep.ilink.swim.models.workflow.FlowMap;
import com.utep.ilink.swim.models.workflow.Model;
import com.utep.ilink.swim.models.workflow.OutputSpec;
import com.utep.ilink.swim.models.workflow.Transformation;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates serializations that serve as inputs to the workflow composer
 * service.
 */
public class ComposerMapper {

    private String _flow_id;
    private FlowInput _flow_input;
    private Datastore _workflow_datastore;
    private Datastore _swim_datastore;

    private ComposerModelCatalog _composer_catalog;
    private ComposerInput _composer_input;

    private Logger _logger;

    /**
     * Composer mapper constructor
     * 
     * @param flow_id        unique identifier for workflow
     * @param flow_input     flow input with inputs, outputs and rules
     * @param workflow_store ODM to workflow database
     */
    public ComposerMapper(String flow_id, FlowInput flow_input, Datastore workflow_store, Datastore swim_datastore) {

        this._logger = LoggerFactory.getLogger(ComposerMapper.class);

        this._flow_id = flow_id;
        this._flow_input = flow_input;
        this._workflow_datastore = workflow_store;
        this._swim_datastore = swim_datastore;

        // Initialize composer objects
        _composer_catalog = new ComposerModelCatalog();
        _composer_input = new ComposerInput();

    }

    /**
     * Generates a composer catalog.
     * 
     * @return ComposerCatalog object.
     */
    public ComposerModelCatalog GenerateComposerCatalog() {
        try {
            // assign the current flow id
            this._composer_catalog.flowid = this._flow_id;

            // Create transformation listing
            _composer_catalog.transformations = GenerateTransformationList();

            // TODO: Move what we have below to another method...

            // Get all the unique models from the flow and add to model list
            List<String> model_ids = _workflow_datastore.getCollection(FlowMap.class).distinct("modelID");

            for (String model_id : model_ids) {
                Model catalog_model = new Model();
                catalog_model.id = model_id;

                // Get model computation information and append to catalog model
                Query<ModelMetadata> squery = _swim_datastore.find(ModelMetadata.class);
                ModelMetadata model_info = squery
                        .filter("_id", model_id)
                        .project("serviceInfo", true)
                        .get();

                catalog_model.computationInfo.put("method", model_info.serviceInfo.serviceMethod);
                catalog_model.computationInfo.put("contentType", model_info.serviceInfo.consumes);
                catalog_model.computationInfo.put("url", model_info.serviceInfo.serviceURL);

                // Get all the flow mappings from that model and add to map (filter by flow,
                // model)
                Query<FlowMap> query = _workflow_datastore.find(FlowMap.class);
                List<FlowMap> flow_maps = query
                        .filter("flowID", this._flow_id)
                        .filter("modelID", model_id)
                        .asList();

                // Insert according to the type (if statement here)
                for (FlowMap flow_map : flow_maps) {
                    if (flow_map.type.equals("input")) {
                        Map<String, Object> e = new HashMap<>();
                        e.put("id", flow_map.catalogID);
                        catalog_model.inputs.add(e);
                    } else if (flow_map.type.equals("output")) {
                        Map<String, Object> e = new HashMap<>();
                        e.put("id", flow_map.catalogID);
                        catalog_model.outputs.add(e);
                    }
                }

                _composer_catalog.models.add(catalog_model);
            }

            // add a hardcoded context for now
            _composer_catalog.context = "http://purl.org/swim/vocab";

            // save in database
            _workflow_datastore.save(_composer_catalog);

            // return the composer catalog object
            return _composer_catalog;

        } catch (Exception e) {
            _logger.error(e.toString());
            return null;
        }

    }

    /**
     * Extract transformation services available and format for composer use.
     * @return a list of transformation services
     */
    private List<Transformation> GenerateTransformationList() {
        try {
            List<Transformation> t_list = new ArrayList<Transformation>();

            // Do the queries here to get transformation data from mongo db
            Query<TransformerMetadata> query = _swim_datastore.find(TransformerMetadata.class);
            List<TransformerMetadata> transformations = query
                    .project("_id", true)
                    .project("serviceInfo", true)
                    .asList();

            // Loop through documents and add to the list
            for (TransformerMetadata t_metadata : transformations) {
                Transformation t_entry = new Transformation();
                t_entry.id = t_metadata._id;
                t_entry.computationInfo.put("method", t_metadata.serviceInfo.serviceMethod);
                t_entry.computationInfo.put("contentType", t_metadata.serviceInfo.consumes);
                t_entry.computationInfo.put("url", t_metadata.serviceInfo.serviceURL);
                t_list.add(t_entry);
            }

            return t_list;

        } catch (Exception e) {
            _logger.error(e.toString());
            return null;
        }

    }

    /**
     * Generates a composer input (request).
     * Note: This method may be optimized with parallel loops...
     * 
     * @return ComposerInput object.
     */
    public ComposerInput GenerateComposerInput() {

        try {
            // assign the current flow id
            this._composer_input.flowid = this._flow_id;

            List<String> exclusions = new ArrayList<String>();

            // extract excluded defaults
            for (Map<String, List<String>> rule : _flow_input.rules) {
                if (rule.containsKey("excludeDefault")) {
                    exclusions = rule.get("excludeDefault");
                }
            }

            // loop through all inputs and append catalog id
            Query<FlowMap> query_flows = _workflow_datastore.find(FlowMap.class);
            query_flows.and(
                    query_flows.criteria("flowID").equal(this._flow_id),
                    query_flows.criteria("type").equal("input"));
            List<FlowMap> flowmaps = query_flows.asList();
            for (FlowMap flowMap : flowmaps) {
                if (!exclusions.contains(flowMap.swimID)) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("id", flowMap.catalogID);
                    _composer_input.inputs.add(e);
                }
            }

            // loop through requested outputs and append to the request
            for (OutputSpec output : _flow_input.outputs) {
                Query<FlowMap> query = _workflow_datastore.find(FlowMap.class);
                query.and(
                        query.criteria("flowID").equal(this._flow_id),
                        query.criteria("swimID").equal(output.varName));
                FlowMap result = query.get();
                Map<String, Object> e = new HashMap<>();
                e.put("id", result.catalogID);
                _composer_input.outputs.add(e);
            }

            // save to database
            _workflow_datastore.save(_composer_input);

            // return composer input object
            return this._composer_input;


        }
        catch(Exception e){
            _logger.error(e.toString());
            // e.printStackTrace(); // debug only
            return null;
        }
    }

}
