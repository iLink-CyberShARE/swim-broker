package com.utep.ilink.swim.services.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 *
 * @author Luis Garnica
 */
public class ServiceConfiguration extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    /* SWIM-DB Credentials */
    @NotEmpty
    private String swimdbuser;
    @NotEmpty
    private String swimdbpassword;
    @NotEmpty
    private String swimdbhost;
    @NotEmpty
    private String swimdbname;

    private String swimdbauth;
    @Min(1)
    @Max(65535)
    private int swimdbport;

    /* Workflow-DB Credentials */
    @NotEmpty
    private String workflowDBUser;
    @NotEmpty
    private String workflowDBPassword;
    @NotEmpty
    private String workflowDBHost;
    @NotEmpty
    private String workflowDBName;
    private String workflowAuthSource;
    @Min(1)
    @Max(65535)
    private int workflowDBPort;

    /* Auth-DB Credentials */
    @NotEmpty
    private String authDBDriver;
    @NotEmpty
    private String authDBHost;
    @Min(1)
    @Max(65535)
    private int authDBPort;
    @NotEmpty
    private String authDBName;
    @NotEmpty
    private String authDBUser;
    @NotEmpty
    private String authDBPassword;

    /* User-DB Credentials */
    @NotEmpty
    private String userDBDriver;
    @NotEmpty
    private String userDBHost;
    @Min(1)
    @Max(65535)
    private int userDBPort;
    @NotEmpty
    private String userDBName;
    @NotEmpty
    private String userDBUser;
    @NotEmpty
    private String userDBPassword;

    /* orchestration services */
    private String composerURL;
    private String swimMergeURL;
    private String serializerURL;
    private String runnerURL;

    /* Getters and Setters */

    /* SWIM-DB */

    @JsonProperty
    public String getSWIMDBUser() {
        return swimdbuser;
    }

    public void setSWIMDBUser(String swimdbuser) {
        this.swimdbuser = swimdbuser;
    }

    @JsonProperty
    public String getSWIMDBPassword() {
        return swimdbpassword;
    }

    public void setSWIMDBPassword(String swimpassword) {
        this.swimdbpassword = swimpassword;
    }

    @JsonProperty
    public String getSWIMDBHost() {
        return swimdbhost;
    }

    public void setSWIMDBHost(String swimdbhost) {
        this.swimdbhost = swimdbhost;
    }

    @JsonProperty
    public String getSWIMDBName() {
        return swimdbname;
    }

    public void setSWIMDBName(String swimdbname) {
        this.swimdbname = swimdbname;
    }

    @JsonProperty
    public int getSWIMDBPort() {
        return swimdbport;
    }

    public void setSWIMDBPort(int swimdbport) {
        this.swimdbport = swimdbport;
    }

    @JsonProperty
    public String getSWIMDBAuth() {
        return swimdbauth;
    }

    public void setSWIMAuth(String swimdbauth) {
        this.swimdbauth = swimdbauth;
    }

    /* Workflow-DB */

    @JsonProperty
    public String getWorkflowDBUser() {
        return workflowDBUser;
    }

    @JsonProperty
    public String getWorkflowDBPassword() {
        return workflowDBPassword;
    }

    @JsonProperty
    public String getWorkflowDBHost() {
        return workflowDBHost;
    }

    @JsonProperty
    public String getWorkflowDBName() {
        return workflowDBName;
    }

    @JsonProperty
    public int getWorkflowDBPort() {
        return workflowDBPort;
    }

    @JsonProperty
    public String getWorkflowAuthSource() {
        return workflowAuthSource;
    }

    /* Auth-DB */

    @JsonProperty
    public String getAuthDBDriver() {
        return this.authDBDriver;
    }

    @JsonProperty
    public String getAuthDBHost() {
        return this.authDBHost;
    }

    @JsonProperty
    public int getAuthDBPort() {
        return this.authDBPort;
    }

    @JsonProperty
    public String getAuthDBName() {
        return this.authDBName;
    }

    @JsonProperty
    public String getAuthDBUser() {
        return this.authDBUser;
    }

    @JsonProperty
    public String getAuthDBPassword() {
        return this.authDBPassword;
    }

    /* User-DB */

    @JsonProperty
    public String getUserDBDriver() {
        return this.userDBDriver;
    }

    @JsonProperty
    public String getUserDBHost() {
        return this.userDBHost;
    }

    @JsonProperty
    public int getUserDBPort() {
        return this.userDBPort;
    }

    @JsonProperty
    public String getUserDBName() {
        return this.userDBName;
    }

    @JsonProperty
    public String getUserDBUser() {
        return this.userDBUser;
    }

    @JsonProperty
    public String getUserDBPassword() {
        return this.userDBPassword;
    }

    /* orchestration services */
    @JsonProperty
    public String getComposerURL() {
        return this.composerURL;
    }

    public void setComposerURL(String composerURL) {
        this.composerURL = composerURL;
    }

    @JsonProperty
    public String getSwimMergeURL() {
        return swimMergeURL;
    }

    public void setSwimMergeURL(String swimMergeURL) {
        this.swimMergeURL = swimMergeURL;
    }

    @JsonProperty
    public String getSerializerURL() {
        return serializerURL;
    }

    public void setSerializerURL(String serializerURL) {
        this.serializerURL = serializerURL;
    }

    @JsonProperty
    public String getRunnerURL() {
        return runnerURL;
    }

    public void setRunnerURL(String runnerURL) {
        this.runnerURL = runnerURL;
    }

}
