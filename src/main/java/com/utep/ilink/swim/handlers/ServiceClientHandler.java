package com.utep.ilink.swim.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.utep.ilink.swim.models.swim.ServiceInfo;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Invocation.Builder;
import org.apache.http.HttpHeaders;


public class ServiceClientHandler {
    
    private Logger _logger;

    private String _payload;
    private String _auth;
    private ServiceInfo _metadata;

    public Form formData;

    /**
     * Webservice client with JSON based payloads and header-based authorization.
     * @param metadata service metadata
     * @param auth authentication string
     * @param payload data payload in json format
     */
    public ServiceClientHandler(ServiceInfo metadata, String auth, String payload){
        this._metadata = metadata;
        this._auth = auth;
        this._payload = payload;
        this.formData = new Form();

        this._logger = LoggerFactory.getLogger(ServiceClientHandler.class);
    }

    /**
     * Calls a web service endpoint with raw data payload and optional auth header.
     * @param useAuth set true to attach authentication header to call
     * @return service response as string
     */
    public String CallService(boolean useAuth) {
        try{

            final Client client = ClientBuilder.newClient();
            final WebTarget webTarget = client.target(_metadata.serviceURL);
            final Invocation.Builder invocationBuilder = webTarget.request(_metadata.consumes);

            // set authentication header (optional)
            if (this._auth != null){
                invocationBuilder.header(HttpHeaders.AUTHORIZATION, _auth);
            }

            Entity<String> requestEntity = Entity.entity(_payload, _metadata.consumes);
            Response response = invocationBuilder.method(_metadata.serviceMethod, requestEntity, Response.class);
            String serviceResponse = response.readEntity(String.class);
            return serviceResponse;

        } catch(Exception e){
            _logger.error(e.toString());
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * Calls a web service endpoint with form based data and optional auth header.
     * @param useAuth set true to attach authentication header to call
     * @return service response as string
     */
    public String CallFormService(boolean useAuth){
        try{
            final Client client = ClientBuilder.newClient();
            final WebTarget target = client.target(_metadata.serviceURL);
            final Builder basicRequest = target.request();  

            // set authentication header (optional)
            if (this._auth != null){
                basicRequest.header(HttpHeaders.AUTHORIZATION, _auth);
            }
            
            Response response = basicRequest.post(Entity.form(this.formData), Response.class);
            String serviceResponse = response.readEntity(String.class);
            return serviceResponse;

        }
        catch(Exception e){
            _logger.error(e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /** Getters and setters */

    public String get_payload() {
        return _payload;
    }

    public void set_payload(String _payload) {
        this._payload = _payload;
    }

    public String get_auth() {
        return _auth;
    }

    public void set_auth(String _auth) {
        this._auth = _auth;
    }

    public ServiceInfo get_metadata() {
        return _metadata;
    }

    public void set_metadata(ServiceInfo _metadata) {
        this._metadata = _metadata;
    }

    
}
