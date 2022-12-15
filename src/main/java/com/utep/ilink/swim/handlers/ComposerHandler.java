package com.utep.ilink.swim.handlers;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.utep.ilink.swim.db.MongoHandler;
import com.utep.ilink.swim.mappers.ComposerMapper;
import com.utep.ilink.swim.mappers.FlowMapper;
import com.utep.ilink.swim.models.swim.ModelMetadata;
import com.utep.ilink.swim.models.swim.SWIMParameter;
import com.utep.ilink.swim.models.swim.TransformerMetadata;
import com.utep.ilink.swim.models.workflow.ComposerInput;
import com.utep.ilink.swim.models.workflow.ComposerModelCatalog;
import com.utep.ilink.swim.models.workflow.Flow;
import com.utep.ilink.swim.models.workflow.FlowInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ComposerHandler {

    private MongoHandler _smh; 
    private MongoHandler _wmh;
    private FlowInput _flowInput;
    private String _flow_id;
    private Logger _logger;

    private Morphia _swimMorphia;
    private Morphia _workflowMorphia;

    private Datastore _swimDatastore;
    private Datastore _workflowDatastore;

    private boolean storeReady = false;

    private String cmc;
    private String ci;
    
    /**
     * Generation of workflow composer inputs.
     * @param payload orchestrator input payload
     * @param swimdb mongo handler to swim database
     * @param workflowdb mongo handler to workflor database
     */
    public ComposerHandler(FlowInput payload, MongoHandler swimdb, MongoHandler workflowdb){
        try {

            this._logger = LoggerFactory.getLogger(ComposerHandler.class);

            // map payload to object
            // this.SetPayload(payload);
            this._flowInput = payload;
            this._flow_id = "";

            // assign database handlers
            _smh = swimdb;
            _wmh = workflowdb;

            // init data stores
            this.initDataStores();

        } catch(Exception e){
            _logger.error(e.toString());
        }
    }

    /**
     * Runs input precproceesing for the workflow composer.
     * @return true if the preprocessing is successful
     */
    public boolean RunPreProcessor(){
        try {

            if(!this.storeReady){
                throw new Exception("Datastores not initialized");
            }

            // Create workflow mappings between SWIM and generic composer
            FlowMapper fm  = new FlowMapper(_flowInput, _swimDatastore, _workflowDatastore);

            // Assign flow id
            this._flow_id = fm.GetFlowID();

            // Create composer inputs (catalog and request)
            ComposerMapper cm = new ComposerMapper(fm.GetFlowID(), _flowInput, _workflowDatastore, _swimDatastore);
            ComposerModelCatalog cmc = cm.GenerateComposerCatalog();
            this.cmc = cmc.toJSONString();
            // ExportFile("composer_catalog.json", cmc.toJSONString());
            ComposerInput ci = cm.GenerateComposerInput();
            this.ci = ci.toJSONString();
            // ExportFile("composer_input.json", ci.toJSONString());

            return true;
        } catch(Exception e){
            _logger.error(e.toString());
            // e.printStackTrace(); // for debug only
            return false;
        }
    }

    /**
     * Initializes object document mappings in morphia.
     */
    private void initDataStores(){
        try {
            // Instantiate morphia objects
            _swimMorphia = new Morphia();
            _workflowMorphia = new Morphia();

            // Map the model classes
            _swimMorphia.map(ModelMetadata.class);
            _swimMorphia.map(SWIMParameter.class);
            _swimMorphia.map(TransformerMetadata.class);
            _workflowMorphia.map(ComposerModelCatalog.class);
            _workflowMorphia.map(Flow.class);

            _swimDatastore = _swimMorphia.createDatastore(_smh.getMongoClient(), _smh.getDBName());
            _workflowDatastore = _workflowMorphia.createDatastore(_wmh.getMongoClient(), _wmh.getDBName());

            storeReady = true;

        } catch (Exception e){
            _logger.error(e.toString());
        }

    }

    /**
     * Exports string content to a file.
     * @param fullname path and name of the file
     * @param content content payload in string format
     * @throws IOException
     */
    public void ExportFile(String fullname, String content) throws IOException {
        Files.write(Paths.get(fullname), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Map the service payload as a workflow flow input
     * @param payload raw input json as string
     */
    public void SetPayload(String payload){
        // map input payload
        ObjectMapper mapper = new ObjectMapper();
        try {
            this._flowInput = (FlowInput) mapper.readValue(payload, FlowInput.class);
        } catch (JsonMappingException e) {
            _logger.error(e.toString());
        } catch (JsonProcessingException e) {
            _logger.error(e.toString());
        }
    }

    public void FinalizeWorkflow(String endTime){
        Query<Flow> query = _workflowDatastore.createQuery(Flow.class)
        .field("_id")
        .equal(_flow_id);
      
        UpdateOperations<Flow> updates = _workflowDatastore.createUpdateOperations(Flow.class)
            .set("metadata.status", "complete").set("metadata.endedAtTime", endTime);
      
        _workflowDatastore.update(query, updates);
    }

    public void CloseConnections(){
        // Close mongo database connections
        _smh.closeConnection();
        _wmh.closeConnection();
    }


    /* Getters and Setters */

    /**
     * Returns a previously generated composer model catalog in json format as string.
     * @return composer model catalog
     */
    public String getCmc() {
        return cmc;
    }

    /**
     * Returns a previously generated composer request in json format as string.
     * @return composer request
     */
    public String getCi() {
        return ci;
    }

    /**
     * Returns the unique identifer for the workflow
     * @return unique workflow identifier
     */
    public String get_flow_id() {
        return _flow_id;
    }


}
