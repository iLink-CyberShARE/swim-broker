package com.utep.ilink.swim.services.controller;

import com.utep.ilink.swim.auth.Auth;
import com.utep.ilink.swim.db.MongoHandler;
import com.utep.ilink.swim.db.RelationalHandler;
import com.utep.ilink.swim.handlers.ComposerHandler;
import com.utep.ilink.swim.handlers.ServiceClientHandler;
import com.utep.ilink.swim.models.swim.ServiceInfo;
import com.utep.ilink.swim.models.workflow.FlowInput;
import com.utep.ilink.swim.services.api.ServiceConfiguration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import com.codahale.metrics.annotation.Timed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/orchestrate")
@Api(value = "/orchestrate", description = "Model Orchestration Endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrchestratorController {

    private ServiceConfiguration _config = null;
    private Logger _logger;

    /**
     * Endpoints for model-to-model orchestration.
     * @param configuration mappings to service configuration file
     */
    public OrchestratorController(ServiceConfiguration configuration) {
        this._config = configuration;
        this._logger = LoggerFactory.getLogger(OrchestratorController.class);
    }

    @POST
    @Timed
    @ApiOperation(
	value = "Submit Request", 
	notes = "Performs model orchestration and transformation workflow")
    public Object run(@HeaderParam("Authorization") String auth, @NotNull @Valid FlowInput payload) {
        try {

            // TODO: start timer here
            long start = System.currentTimeMillis();

            // validate authentication
            RelationalHandler authdb = new RelationalHandler(_config.getAuthDBDriver());
            authdb.setDataSource(_config.getAuthDBHost(), String.valueOf(_config.getAuthDBPort()), _config.getAuthDBName());
            Auth identity = new Auth(authdb, _config.getAuthDBUser(), _config.getAuthDBPassword());
            if (!identity.isAuthValid(auth)) {
                throw new Exception("Invalid Authorization Token");
            }

            // TODO: json payload validation and sanitation here (or build the model and validate against it)

            // open mongo connection SWIM model database
            MongoHandler smh = new MongoHandler(_config.getSWIMDBUser(), _config.getSWIMDBPassword(),
                    _config.getSWIMDBName(), _config.getSWIMDBHost(), _config.getSWIMDBPort(), _config.getSWIMDBAuth());

            // check the connection was established
            if(!smh.isConnected()){
                throw new Exception("Connection to SWIM Modeling database could not be established");
            }

            // Open mongo connection to Workflow database
            MongoHandler wmh = new MongoHandler(_config.getWorkflowDBUser(), _config.getWorkflowDBPassword(),
                    _config.getWorkflowDBName(), _config.getWorkflowDBHost(), _config.getWorkflowDBPort(),
                    _config.getWorkflowAuthSource());

            if(!smh.isConnected()){
                throw new Exception("Connection to Workflow database could not be established");
            }

            // generate composer inputs
            System.out.println("Performing composer preprocessing operations...");
            ComposerHandler ch = new ComposerHandler(payload, smh, wmh);
            Boolean isSuccess = ch.RunPreProcessor();
            // ch.ExportFile("./outputs/composer_request.json", ch.getCi());  (DEBUG)
            // ch.ExportFile("./outputs/composer_catalog.json", ch.getCmc());  (DEBUG)

            if(!isSuccess) {
                throw new Exception("Error generating composer inputs");
            }

            // composer webservice setup
            ServiceInfo composerMeta = new ServiceInfo();
            composerMeta.serviceURL = _config.getComposerURL();
            composerMeta.serviceMethod = "POST";
            composerMeta.produces = "application/json";

            // generate workflow plan to cwl (call workflow composer)
            System.out.println("Performing workflow composition...");
            ServiceClientHandler sch = new ServiceClientHandler(composerMeta, auth, null);
            sch.formData.param("request", ch.getCi());
            sch.formData.param("modelcatalog", ch.getCmc());
            String schResponse = sch.CallFormService(false);
            // ch.ExportFile("./outputs/workflow_plan.json", schResponse); (DEBUG)
            // System.out.println(schResponse);

            // convert response to json object for further manipulation
            JSONObject schJSON  = new JSONObject(schResponse); // json

            // run workflow serializer
            ServiceInfo serializerMeta = new ServiceInfo();
            serializerMeta.serviceURL = _config.getSerializerURL();
            serializerMeta.serviceMethod = "PUT";
            serializerMeta.produces = "application/json";
            serializerMeta.consumes = "application/json";

            // construct the serializer payload 
            JSONObject serializerJSON = new JSONObject()
                                .put("workflow", schJSON)
                                .put("flowid", ch.get_flow_id());

            String serializerPayload = serializerJSON.toString().replaceAll("\\\\", "");
            
            System.out.println("Serializing Workflow plan to CWL..."); 
            ServiceClientHandler serializerService = new ServiceClientHandler(serializerMeta, auth, serializerPayload);
            String serializerResponse = serializerService.CallService(true);
            // System.out.println(serializerResponse);

            if (serializerResponse.equals("You have the power!!!")) {
                throw new Exception("Serializer Error:" + serializerResponse);
            }

            // run workflow 
            ServiceInfo runnerMeta = new ServiceInfo();
            runnerMeta.serviceURL = _config.getRunnerURL();
            runnerMeta.serviceMethod = "PUT";
            runnerMeta.consumes = "application/json";
            runnerMeta.produces = "application/json";

            JSONObject runnerJSON = new JSONObject()
                                    .put("flowid", ch.get_flow_id());
            String runnerPayload = runnerJSON.toString();
            

            // TODO: invoke thread and make this async
            System.out.println("Running CWL workflow execution...");
            ServiceClientHandler runnerService = new ServiceClientHandler(runnerMeta, auth, runnerPayload);
            String runnerResponse = runnerService.CallService(true);
            // System.out.println(runnerResponse);

            if (runnerResponse.equals("You have the power!!!")){
                throw new Exception("CWL Runner Error:" + runnerResponse);
                // System.out.println("check here");
            }

            // swim-merge webservice setup
            ServiceInfo mergeMeta = new ServiceInfo();
            mergeMeta.serviceURL  = _config.getSwimMergeURL();
            mergeMeta.serviceMethod = "POST";
            mergeMeta.produces = "application/json";
            mergeMeta.consumes  = "application/json";

            // run swim-merge (temporal sync)
            // TODO: move to result request endpoint to check if workflow finished and return the requested outputs
            System.out.println("Performing output merge...");
            ServiceClientHandler mergeService = new ServiceClientHandler(mergeMeta, auth, runnerPayload);
            String mergeResponse = mergeService.CallService(true);

            // End time here and assign workflow as complete
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("Worlflow TimeLapse: " + timeElapsed + "ms");
            LocalDateTime dateObj = LocalDateTime.now();
            ch.FinalizeWorkflow(dateObj.toString());
            ch.CloseConnections();

            // return final results from swim merge (sync)
            // String message = "{\"message\": \"" + "success" + "\"}";
            return Response.ok().entity(mergeResponse).build();

        } catch (Exception e) {
            String exception = "An internal error ocurred.";
            // e.printStackTrace(); // debug only
            if (e.getMessage() != null) {
                exception = e.getMessage();
            }
            _logger.error(e.toString());
            String message = "{\"message\": \"" + exception + "\"}";
            return Response.serverError().entity(message).build();
        }

    }

}
