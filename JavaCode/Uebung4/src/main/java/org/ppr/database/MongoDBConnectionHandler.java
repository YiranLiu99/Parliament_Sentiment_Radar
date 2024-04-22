package org.ppr.database;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.ppr.xmlAnalyse.XMLReader;
import org.ppr.xmlAnalyse.data.impl.Speaker_File_Impl;
import org.ppr.xmlAnalyse.data.impl.Speech_File_Impl;

import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

/**
 * Class to handle MongoDB Connection.
 */
public class MongoDBConnectionHandler {

    private static final String PLEASE_SEND_IP = "No IP or port";
    private static final String PWD_UN_ERR = "Username and password not match";
    private static final String PLEASE_INSTANCE_MONGOCLIENT = "Please instance MongoClient";
    private static final String PLEASE_SEND_MONGO_REPOSITORY = "Pealse choose a mongoDB";
    private static final String DELETE_MONGO_REPOSITORY_EXCEPTION = "Exception by deleting mongoDB";
    private static final String DELETE_MONGO_REPOSITORY_SUCCESS = "Delete multi mongoDB successfully";
    private static final String NOT_DELETE_MONGO_REPOSITORY = "Fail to delete mongoDB";
    private static final String DELETE_MONGO_REPOSITORY = "Delete mongoDB successfully：";
    private static final String CREATE_MONGO_COLLECTION_NOTE = "Please choose a mongoDB collection";
    private static final String NO_THIS_MONGO_DATABASE = "mongoDB not found";
    private static final String CREATE_MONGO_COLLECTION_SUCCESS = "Create mongoDB collection successfully";
    private static final String CREATE_MONGO_COLLECTION_EXCEPTION = "Fail to create mongoDB";
    private static final String NOT_CREATE_MONGO_COLLECTION = "Fail to create mongoDB collection";
    private static final String CREATE_MONGO_COLLECTION_SUCH = "Create mongoDB collection successfully：";
    private static final String INSERT_DOCUMEN_EXCEPTION = "Fail to insert the document";
    private static final String INSERT_DOCUMEN_SUCCESSS = "Insert the document successfully";
    private static final String DELETE_COLLECTION_EXCEPTION = "Fail to delete the Collection";
    private static final String DELETE_COLLECTION_SUCCESS = "Delete the Collection successfully";

    private static final Logger logger = Logger.getLogger(MongoDBConnectionHandler.class);

    private MongoDBConfig dbConfig;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> speechCollection;
    private MongoCollection<Document> speakersCollection;
    private MongoCollection<Document> countProgressCollection;


    /**
     * Constructor of MongoDBConnectionHandler class.
     *
     * @throws IOException IOException
     * @author Yiran
     */
    public MongoDBConnectionHandler() throws IOException {
        getMongoConnect();
        createAllMongoCollection();
        speechCollection = getMongoCollection("speeches");
        speakersCollection = getMongoCollection("speakers");
        countProgressCollection = getMongoCollection("countProgress");
    }


    /**
     * Method to get MongoDB connection.
     *
     * @author Yiran
     * @author Philipp (edited)
     */
    public void getMongoConnect() throws IOException {
        dbConfig = new MongoDBConfig("src/main/resources/PRG_WiSe22_Group_3_1.txt");

        // Define credentials:
        MongoCredential credential = MongoCredential.createScramSha1Credential(dbConfig.getMongoUsername(), dbConfig.getMongoDatabase(), dbConfig.getMongoPassword().toCharArray());

        // Defining hostname and port:
        ServerAddress seed = new ServerAddress(dbConfig.getMongoHostname(), dbConfig.getMongoPort());
        List<ServerAddress> seeds = new ArrayList<>(0);
        seeds.add(seed);

        // Defining options:
        MongoClientOptions options = MongoClientOptions.builder()
                .connectionsPerHost(10)
                .socketTimeout(50000)
                .maxWaitTime(1000)
                .connectTimeout(10000)
                .sslEnabled(false)
                .build();

        if (null == seed) {
            logger.error(PLEASE_SEND_IP);
        }

        if (null == credential) {
            logger.error(PWD_UN_ERR);
        }

        // Connect to database:
        dbConfig = new MongoDBConfig("src/main/resources/PRG_WiSe22_Group_3_1.txt");
        mongoClient = new MongoClient(seeds, credential, options);
        database = mongoClient.getDatabase("PRG_WiSe22_Group_3_1");
        System.out.println("Connect to " + dbConfig.getMongoDatabase() + " on " + dbConfig.getMongoHostname());
    }


    /**
     * Method to create a collection in database.
     *
     * @param collectionName collection name
     * @author Yiran
     */
    public void createMongoCollection(String collectionName) {

        if (null == mongoClient) logger.info(PLEASE_INSTANCE_MONGOCLIENT);

        if (null == collectionName) {
            logger.info(CREATE_MONGO_COLLECTION_NOTE);
        }

        if (null == database) logger.info(NO_THIS_MONGO_DATABASE);

        try {
            MongoIterable<String> collectionNameList = database.listCollectionNames();
            for (String name : collectionNameList) {
                if (name.equals(collectionName)) {
                    logger.info(NOT_CREATE_MONGO_COLLECTION);
                }
            }
            database.createCollection(collectionName);
            logger.info(CREATE_MONGO_COLLECTION_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CREATE_MONGO_COLLECTION_EXCEPTION);
        }
    }

    /**
     * Method to create all collections.
     *
     * @author Yiran
     */
    public void createAllMongoCollection() {
        if (!checkMongoCollectionExists("speeches")) {
            createMongoCollection("speeches");
        }
        if (!checkMongoCollectionExists("speakers")) {
            createMongoCollection("speakers");
        }
        if (!checkMongoCollectionExists("countProgress")) {
            createMongoCollection("countProgress");
        }
    }


    /**
     * Method to check if a collection exists in database.
     *
     * @param collectionName collection name
     * @return boolean
     * @author Yiran
     */
    public boolean checkMongoCollectionExists(String collectionName) {
        boolean collectionExists = database.listCollectionNames().into(new ArrayList<String>()).contains(collectionName);
        return collectionExists;
    }


    /**
     * Method to delete a collection in database.
     *
     * @param collectionName collection name
     * @author Yiran
     * @author Philipp (edited)
     */
    public void deleteMongoCollectionContent(String collectionName) {
        if (null == database) logger.info(NO_THIS_MONGO_DATABASE);

        try {
            MongoIterable<String> collectionNameList = database.listCollectionNames();
            boolean findCollection = false;
            for (String name : collectionNameList) {
                if (name.equals(collectionName)) {
                    findCollection = true;
                    break;
                }
            }
            if (!findCollection) {
                logger.info(DELETE_COLLECTION_EXCEPTION);
            }
            database.getCollection(collectionName).deleteMany(new BasicDBObject());
            logger.info(DELETE_COLLECTION_SUCCESS + String.join(",", collectionName));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(DELETE_COLLECTION_EXCEPTION);
        }
    }


    /**
     * Method to get a collection.
     *
     * @param collection collection name
     * @return collection
     * @author Yiran
     */
    public MongoCollection<Document> getMongoCollection(String collection) {

        if (null == mongoClient) return null;

        if (StringUtils.isBlank(collection)) return null;

        MongoCollection<Document> collectionDocument = database.getCollection(collection);

        if (null == collectionDocument) return null;

        return collectionDocument;
    }


    /**
     * Reset a document from countProgressCollection. Set Total, count as 0.
     *
     * @param documentName The document to be updated.
     * @author Yiran
     */
    public void resetCountProgressCollection(String documentName) {
        updateDocument(countProgressCollection, documentName, "Total", 0);
        updateDocument(countProgressCollection, documentName, "count", 0);
    }

    /**
     * Reset the whole countProgressCollection. Set Total, count as 0.
     *
     * @author Yiran
     */
    public void resetTotalCountProgressCollection() {
        FindIterable<Document> countProgressDocuments = countProgressCollection.find();
        for (Document document : countProgressDocuments) {
            String name = String.valueOf(document.get("_id"));
            updateDocument(countProgressCollection, name, "Total", 0);
            updateDocument(countProgressCollection, name, "count", 0);
        }
    }


    /**
     * Update the count number of a document from countProgressCollection.
     *
     * @param documentName The document to be updated.
     * @param option To update total or count?
     * @param number The number to update.
     * @author Yiran
     */
    public void updateCountProgressCollection(String documentName, String option, Integer number) {
        updateDocument(countProgressCollection, documentName, option, number);
    }


    /**
     * Insert all data into the collections.
     *
     * @param reader XMLReader object
     * @author Yiran
     * @author Philipp (edited)
     */
    public void insertAllDoucument(XMLReader reader) {
        Map<String, Speech_File_Impl> speeches = reader.getSpeechMap();
        Map<String, Speaker_File_Impl> speakers = reader.getSpeakerMap();

        updateCountProgressCollection("countSpeakersData", "Total", speakers.size());
        updateCountProgressCollection("countSpeechesData", "Total", speeches.size());

        System.out.println("Inserting all Speeches.");
        int countSpeech = 0;
        for (String key : speeches.keySet()) {
            try {
                speechCollection.insertOne(speeches.get(key).createDocument());
                countSpeech += 1;
                if (countSpeech % 10 == 0 || countSpeech == speeches.size()) {
                    updateCountProgressCollection("countSpeechesData", "count", countSpeech);
                }
            } catch (Exception e) {
                System.out.println("Fehler: Diese Rede ist schon vorhanden.");
                countSpeech += 1;
                if (countSpeech % 10 == 0 || countSpeech == speeches.size()) {
                    updateCountProgressCollection("countSpeechesData", "count", countSpeech);
                }
            }
        }
        System.out.println("Speeches ready.");

        System.out.println("Inserting all Speakers.");
        int countSpeaker = 0;
        for (String key : speakers.keySet()) {
            try {
                speakersCollection.insertOne(speakers.get(key).createDocument());
                countSpeaker += 1;
                if (countSpeaker % 10 == 0 || countSpeaker == speakers.size()) {
                    updateCountProgressCollection("countSpeakersData", "count", countSpeaker);
                }
            } catch (Exception e) {
                System.out.println("Fehler: Dieser Sprecher ist schon vorhanden.");
                countSpeaker += 1;
                if (countSpeaker % 10 == 0 || countSpeaker == speakers.size()) {
                    updateCountProgressCollection("countSpeakersData", "count", countSpeaker);
                }
            }
        }
        System.out.println("Speakers ready.");
    }


    /**
     * Method to read all documents in a collection.
     *
     * @param collection collection
     * @return documents
     * @author Yiran
     * @author Philipp (edited)
     */
    public FindIterable<Document> readAllDocumentInCollection(MongoCollection<Document> collection, String key) {
        FindIterable<Document> documents = collection.find(or(eq(key, ""), eq(key, null)));
        return documents;
    }


    /**
     * Method to delete a document by id.
     *
     * @param collection collection
     * @param id         document id
     * @return boolean
     * @author Yiran
     */
    public boolean deleteDocument(MongoCollection<Document> collection, String id) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", id);
        DeleteResult dResult = collection.deleteOne(whereQuery);
        return dResult.wasAcknowledged();
    }


    /**
     * Method to update a document by id.
     *
     * @param collection collection
     * @param id         document id
     * @param name       new key name
     * @param value      new value
     * @author Philipp
     */
    public void updateDocument(MongoCollection<Document> collection, String id, String name, Object value) {

        if (null == collection) return;

        try {
            collection.updateOne(new Document("_id", id), Updates.set(name, value));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(INSERT_DOCUMEN_EXCEPTION);
        }
    }



    /**
     * Method to update a map to a document by id.
     * @param collection collection
     * @param id         document id
     * @param name       new key name
     * @param value      new value (map)
     * @author Philipp
     * @author Yiran (edited)
     */
    public void updateDocumentMap(MongoCollection<Document> collection, String id, String name, HashMap value) {

        if (null == collection) return;

        try {
            collection.updateOne(new Document("_id", id), Updates.set(name, value));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(INSERT_DOCUMEN_EXCEPTION);
        }
    }


    /**
     * Method to count speeches of a speaker.
     *
     * @param collection collection
     * @param speakerID  id of speaker
     * @return number of speeches
     * @author Philipp
     */
    public int countSpeeches(MongoCollection<Document> collection, String speakerID) {
        BasicDBObject query = new BasicDBObject();
        query.put("speakerID", speakerID);
        return (int) collection.countDocuments(query);
    }


    /**
     * Method to sum speech length of a speaker.
     *
     * @param collection collection
     * @param speakerID  id of speaker
     * @return length of speeches
     * @author Philipp (edited)
     * @author Yiran
     */
    public Integer aggregateSpeechText(MongoCollection<Document> collection, String speakerID) {
        BasicDBObject query = new BasicDBObject();
        query.put("speakerID", speakerID);
        Document totalLengthDoc = collection.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("speakerID", speakerID)),
                        Aggregates.group("$speakerID", Accumulators.sum("totalLength", "$textLength"))
                )
        ).first();
        if (totalLengthDoc == null) {
            return 0;
        } else {
            return (int) totalLengthDoc.get("totalLength");
        }
    }


    /**
     * Method to check if document exists in database.
     * @author Philipp
     * @param collectionname collectionname
     * @param id id of document
     * @return boolean
     */
    public boolean checkIfDocumentExists(String collectionname, String id) {
        MongoCollection<Document> collection = this.database.getCollection(collectionname);
        FindIterable<Document> iterable = collection.find(new Document("_id", id));
        return iterable.first() != null;
    }


    /**
     * Getter for speechCollection.
     *
     * @return collection
     * @author Yiran
     */
    public MongoCollection<Document> getSpeechCollection() {
        return speechCollection;
    }

    /**
     * Getter for speakersCollection.
     *
     * @return collection
     * @author Yiran
     */
    public MongoCollection<Document> getSpeakersCollection() {
        return speakersCollection;
    }
}
