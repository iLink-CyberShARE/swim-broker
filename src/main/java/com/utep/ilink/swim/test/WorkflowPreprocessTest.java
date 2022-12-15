package com.utep.ilink.swim.test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.utep.ilink.swim.db.MongoHandler;
import com.utep.ilink.swim.mappers.ComposerMapper;
import com.utep.ilink.swim.mappers.FlowMapper;
import com.utep.ilink.swim.models.swim.ModelMetadata;
import com.utep.ilink.swim.models.swim.SWIMOutput;
import com.utep.ilink.swim.models.swim.SWIMParameter;
import com.utep.ilink.swim.models.swim.TransformerMetadata;
import com.utep.ilink.swim.models.workflow.ComposerInput;
import com.utep.ilink.swim.models.workflow.ComposerModelCatalog;
import com.utep.ilink.swim.models.workflow.Flow;
import com.utep.ilink.swim.models.workflow.FlowInput;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowPreprocessTest {

    private static final String inputPath = "src/main/resources/broker_use_case.json";

    // TODO: load all this from the configuration file and remove connection strings from here
    // SWIM Modeling Database Connection Parameters
    private static final String mongoUser = "ilinkadmin";
    private static final String mongoPassword = "ad54YpKz%40ilink";
    private static final String mongoHost = "129.108.18.45";
    private static final String mongoDB = "swim-dev-2";
    private static final int mongoPort = 27017;
    private static final String mongoAuthSource = "admin";

    // Workflow Database Connection Parameters
    private static final String rmongoUser = "ilinkadmin";
    private static final String rmongoPassword = "ad54YpKz%40ilink";
    private static final String rmongoHost = "129.108.18.45";
    private static final String rmongoDB = "workflow";
    private static final int rmongoPort = 27017;
    private static final String rmongoAuthSource = "admin";

    public static void main(String[] args){
        try{
            System.out.println("OK HERE WE GO....");

            // read sample input from json file (will come on webservice request later on)
            FileReader rawFlowInput;
            rawFlowInput = new FileReader(inputPath);
            ObjectMapper mapper = new ObjectMapper();
            FlowInput flowInput = (FlowInput) mapper.readValue(rawFlowInput, FlowInput.class);
            rawFlowInput.close();

            // Open mongo connection to SWIM model database
            MongoHandler smh = new MongoHandler(mongoUser, mongoPassword, mongoDB, mongoHost, mongoPort, mongoAuthSource);

            // Open mongo connection to Workflow database
            MongoHandler wmh = new MongoHandler(rmongoUser, rmongoPassword, rmongoDB, rmongoHost, rmongoPort, rmongoAuthSource);

            // Instantiate morphia objects
            Morphia swim_morphia = new Morphia();
            Morphia workflow_morphia = new Morphia();

            // Map the model classes
            swim_morphia.map(ModelMetadata.class);
            swim_morphia.map(SWIMParameter.class);
            swim_morphia.map(TransformerMetadata.class);
            workflow_morphia.map(ComposerModelCatalog.class);
            workflow_morphia.map(Flow.class);

            // Create the datastores
            Datastore swim_datastore = swim_morphia.createDatastore(smh.getMongoClient(), mongoDB);
            Datastore workflow_datastore = workflow_morphia.createDatastore(wmh.getMongoClient(), rmongoDB);

            // Create workflow mappings between SWIM and generic composer
            FlowMapper fm  = new FlowMapper(flowInput, swim_datastore, workflow_datastore);

            // Create composer inputs (catalog and request)
            ComposerMapper cm = new ComposerMapper(fm.GetFlowID(), flowInput, workflow_datastore, swim_datastore);
            ComposerModelCatalog cmc = cm.GenerateComposerCatalog();
            ExportFile("composer_catalog.json", cmc.toJSONString());
            ComposerInput ci = cm.GenerateComposerInput();
            ExportFile("composer_input.json", ci.toJSONString());

            // Close mongo database connections
            smh.closeConnection();
            wmh.closeConnection();

        } catch(Exception e){
            e.printStackTrace();
        } 
    } 
    
    public static List<SWIMParameter> TestSWIMParameters(Datastore swim_datastore) {
        Query<SWIMParameter> query = swim_datastore.find(SWIMParameter.class);
        List<SWIMParameter> params = query.filter("modelID", "7b7ac93638f711ec8d3d0242")
        .asList();
        System.out.println(params.get(0).paraminfo.get(0).paramLabel);
        System.out.println("Test End");
        return params;
    }

    public static List<SWIMOutput> TestSWIMOutputs(Datastore swim_datastore) {
        Query<SWIMOutput> query = swim_datastore.find(SWIMOutput.class);
        List<SWIMOutput> outputs = query.filter("modelID", "7b7ac93638f711ec8d3d0242")
        .asList();
        System.out.println("Test End");
        return outputs;
    }

    public static void ExportFile(String fullname, String content) throws IOException {
        Files.write(Paths.get(fullname), content.getBytes(StandardCharsets.UTF_8));
    }

}
