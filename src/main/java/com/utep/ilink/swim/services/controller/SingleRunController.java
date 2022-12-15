package com.utep.ilink.swim.services.controller;

import com.utep.ilink.swim.auth.Auth;
import com.utep.ilink.swim.db.MongoHandler;
import com.utep.ilink.swim.db.RelationalHandler;
import com.utep.ilink.swim.handlers.ModelServiceHandler;
import com.utep.ilink.swim.handlers.ServiceClientHandler;
import com.utep.ilink.swim.models.swim.ServiceInfo;
import com.utep.ilink.swim.services.api.ServiceConfiguration;

import io.swagger.annotations.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import com.utep.ilink.swim.models.swim.UserSpecification;
import com.utep.ilink.swim.mappers.JSONMapper;

@Path("/singlerun")
@Api(value = "/singlerun", description = "Single Run Endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SingleRunController {

    private ServiceConfiguration _config = null;
    private Logger _logger;

    /**
     * Endpoints for single model run operations
     * 
     * @param configuration mappings to service configuration file
     */
    public SingleRunController(ServiceConfiguration configuration) {
        this._config = configuration;
        this._logger = LoggerFactory.getLogger(OrchestratorController.class);
    }

    @POST
    @Timed
    // @ApiOperation(value = "Submit Request", notes = "Performs single model execution")
    public Object run(@HeaderParam("Authorization") String auth, String payload) {

        try {
            // validate authentication
            RelationalHandler authdb = new RelationalHandler(_config.getAuthDBDriver());
            authdb.setDataSource(_config.getAuthDBHost(), String.valueOf(_config.getAuthDBPort()),
                    _config.getAuthDBName());
            Auth identity = new Auth(authdb, _config.getAuthDBUser(), _config.getAuthDBPassword());
            if (!identity.isAuthValid(auth)) {
                throw new Exception("Invalid Authorization Token");
            }

            // json payload validation and sanitation
            UserSpecification userSpecification = (UserSpecification) JSONMapper.toObject(payload,
                    UserSpecification.class);
            if (userSpecification == null || userSpecification.getModelSettings() == null
                    || userSpecification.getModelSettings().size() == 0 || userSpecification.getModelID() == null
                    || userSpecification.getModelID().equals("")) {
                throw new Exception("Invalid execution payload");
            }

            // open mongo connection SWIM model database
            MongoHandler smh = new MongoHandler(_config.getSWIMDBUser(), _config.getSWIMDBPassword(),
                    _config.getSWIMDBName(), _config.getSWIMDBHost(), _config.getSWIMDBPort(), _config.getSWIMDBAuth());

            // check the connection was established
            if (!smh.isConnected()) {
                throw new Exception("Connection to SWIM Modeling database could not be established");
            }

            // get service metadata
            ServiceInfo serviceMeta = this.GetServiceMetadata(userSpecification.getModelID(), smh);

            // invoke the model webservice
            ServiceClientHandler sch = new ServiceClientHandler(serviceMeta, auth, payload);
            String serviceResponse = sch.CallService(true);

            // close mongo connection
            smh.closeConnection();

            if (serviceResponse != null)
                return Response.ok(serviceResponse).build();

            String message = "{\"message\": \"" + "Error in model service call" + "\"}";
            return Response.serverError().entity(message).build();

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

    /**
     * Fetch metadata needed for invocation of a modeling service in SWIM
     * 
     * @param modelID unique scientific model identifier
     * @param smh     mongo database handler
     * @return model service metadata
     */
    private ServiceInfo GetServiceMetadata(String modelID, MongoHandler smh) {
        ServiceInfo metadata = new ServiceInfo();
        ModelServiceHandler msh = new ModelServiceHandler(smh);
        metadata = msh.GetModelServiceInfo(modelID);
        return metadata;
    }

}
