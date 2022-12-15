package com.utep.ilink.swim.handlers;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.utep.ilink.swim.db.MongoHandler;
import com.utep.ilink.swim.models.swim.ModelMetadata;
import com.utep.ilink.swim.models.swim.ServiceInfo;


public class ModelServiceHandler {

    private Logger _logger;
    private MongoHandler _smh; 
    private Morphia _swimMorphia;
    private Datastore _swimDatastore;
    private boolean storeReady = false;

    /**
     * Handle model service operations with data store
     * @param smh mongo connection
     */
    public  ModelServiceHandler(MongoHandler smh){
        this._logger = LoggerFactory.getLogger(ModelServiceHandler.class);
        _smh = smh;
        initDataStores();
    }

    /**
     * Initializes object mappings in morphia
     */
    private void initDataStores(){
        try{
            // Instantiate morphia objects
            _swimMorphia = new Morphia();
            _swimMorphia.map(ModelMetadata.class);

            _swimDatastore = _swimMorphia.createDatastore(_smh.getMongoClient(), _smh.getDBName());
            storeReady = true;

        } catch(Exception e){
            storeReady = false;
            _logger.error(e.toString());
        }

    }

    /**
     * Fetch metadata needed for invocation of a modeling service in SWIM
     * @param modelID unique model identifier
     * @return modeling service metadata
     */
    public ServiceInfo GetModelServiceInfo(String modelID){
        try {

            if(!this.storeReady){
                throw new Exception("Datastores not initialized");
            }
            Query<ModelMetadata> squery = _swimDatastore.find(ModelMetadata.class);
            ModelMetadata model_info = squery
                    .filter("_id", modelID)
                    .project("serviceInfo", true)
                    .get();

            if (model_info == null){
                throw new Exception("Error fetching model service metadata"); 
            }
            
            return model_info.serviceInfo;

        } catch (Exception e) {
            _logger.error(e.toString());
            return null;
        }
    }
    

}
