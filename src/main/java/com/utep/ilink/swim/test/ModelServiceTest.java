package com.utep.ilink.swim.test;

import com.utep.ilink.swim.db.MongoHandler;
import com.utep.ilink.swim.handlers.ModelServiceHandler;
import com.utep.ilink.swim.models.swim.ServiceInfo;

public class ModelServiceTest {

    // TODO: load all this from the configuration file and remove connection strings from here

    // SWIM Modeling Database Connection Parameters
    private static final String mongoUser = "ilinkadmin";
    private static final String mongoPassword = "ad54YpKz%40ilink";
    private static final String mongoHost = "129.108.18.45";
    private static final String mongoDB = "swim-dev-2";
    private static final int mongoPort = 27017;
    private static final String mongoAuthSource = "admin";


    public static void main(String[] args){

        System.out.println("Starting Test...");

        // Test model ID
        String modelID = "7b7ac93638f711ec8d3d0242";

        // Open mongo connection to SWIM model database
        MongoHandler smh = new MongoHandler(mongoUser, mongoPassword, mongoDB, mongoHost, mongoPort, mongoAuthSource);

        ServiceInfo metadata = new ServiceInfo();
        ModelServiceHandler msh = new ModelServiceHandler(smh);
        metadata = msh.GetModelServiceInfo(modelID);

        System.out.println(metadata.toString());

        // String env_test = System.getenv("Path");

        // System.out.println(env_test);

        System.out.println("Test End...");

    }
    
}
