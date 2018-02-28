package org.apache.airavata.datacat.registry.util;

import com.mongodb.*;
import org.apache.airavata.datacat.registry.MongoRegistryImpl;
import org.apache.airavata.datacat.registry.RegistryException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataMigrator {

    private final static Logger logger = LoggerFactory.getLogger(MongoRegistryImpl.class);

    private static final String MONGO_SERVER_URL = "mongo.server.url";
    private static final String MONGO_DB_NAME = "mongo.db.name";
    private static final String MONGO_COLLECTION_NAME = "mongo.collection.name";
    private static final String MONGO_PRIMARY_FIELD = "mongo.primary.field";

    private String primaryKey = RegistryProperties.getInstance().getProperty(MONGO_PRIMARY_FIELD,"id");
    private String mongoServerUrl = RegistryProperties.getInstance().getProperty(MONGO_SERVER_URL, "");
    private String mongoDBName = RegistryProperties.getInstance().getProperty(MONGO_DB_NAME, "datacat-db");
    private String mongoCollectionName = RegistryProperties.getInstance().getProperty(MONGO_COLLECTION_NAME, "datacat-collection");

    private MongoClient mongoClient = null;
    private DB mongoDB;
    private DBCollection collection;

    public DataMigrator(){
        mongoClient = new MongoClient(new MongoClientURI(mongoServerUrl));
        mongoDB = mongoClient.getDB(mongoDBName);
        collection = mongoDB.getCollection(mongoCollectionName);
//        collection.remove(new BasicDBObject());
        collection.dropIndexes();
        initIndexes();
        logger.debug("New Mongo Client created with [" + mongoServerUrl + "]");
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes() {
        if(!primaryKey.equals("_id")) { //Not using the default MongoDB primary key
            collection.createIndex(new BasicDBObject(primaryKey, 1), new BasicDBObject("unique", true));
        }
    }

    public void getAll() throws RegistryException, ParseException {

        DBObject query = new BasicDBObject("ExecutionEnvironment.FinTimeStamp", new BasicDBObject("$exists", false));
        DBCursor dbObjects = collection.find(query);
        while (dbObjects.hasNext()) {
            DBObject dbObject = dbObjects.next();
            DBObject executionEnvironment = (DBObject) dbObject.get("ExecutionEnvironment");
            Object finTime = executionEnvironment.get("FinTime");
            if (finTime != null) {
                String time = (String) finTime;
                DateFormat formatter = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
                Date d = formatter.parse(time);

                executionEnvironment.put("FinTimeStamp", d.getTime());
                dbObject.put("ExecutionEnvironment", executionEnvironment);

                DBObject updateQuery = new BasicDBObject("_id", dbObject.get("_id"));
                collection.update(updateQuery, dbObject);
                System.out.println(dbObject.toString());
            }
        }
    }
    public static void main(String args[]) throws RegistryException, ParseException {
        DataMigrator migrator = new DataMigrator();
        migrator.getAll();
    }
}
